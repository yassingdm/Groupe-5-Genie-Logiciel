package eidd.grp5.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import eidd.grp5.model.User;
import eidd.grp5.repository.ReservationRepository;

public class ReservationService {

    private final ReservationRepository reservationRepository;
    // Observer pattern: this list allows external listeners to react to reservation lifecycle events.
    private final List<ReservationObserver> observers = new ArrayList<>();

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Creates a new reservation with the minimum required data.
     */
    public Reservation createReservation(Reservation reservation) {
        validateReservation(reservation);

        if (hasSchedulableData(reservation)
                && !isRoomAvailable(reservation.getRoom().getId(), reservation.getStartDate(), reservation.getEndDate(), null)) {
            throw new IllegalStateException("Room is not available for the requested time slot");
        }

        if (reservation.getCreationDate() == null) {
            reservation.setCreationDate(LocalDateTime.now());
        }
        if (reservation.getStatus() == null) {
            reservation.setStatus(Reservation.Status.PENDING);
        }
        if (isBlank(reservation.getReference())) {
            reservation.setReference(generateReference(reservation));
        }
        Reservation saved = reservationRepository.save(reservation);
        notifyObservers(ReservationEventType.CREATED, saved);
        return saved;
    }

    public Reservation updateReservation(Reservation reservation) {
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation id must not be null for update");
        }

        validateReservation(reservation);
        if (hasSchedulableData(reservation)
                && !isRoomAvailable(reservation.getRoom().getId(), reservation.getStartDate(), reservation.getEndDate(),
                        reservation.getId())) {
            throw new IllegalStateException("Room is not available for the requested time slot");
        }

        if (isBlank(reservation.getReference())) {
            reservation.setReference(generateReference(reservation));
        }

        Reservation saved = reservationRepository.save(reservation);
        notifyObservers(ReservationEventType.UPDATED, saved);
        return saved;
    }

    public Reservation modifyReservationAsAdmin(User actor, Reservation reservation) {
        if (actor == null || actor.getId() == null) {
            throw new IllegalArgumentException("actor must not be null");
        }
        // Only admin users can modify reservations that are not theirs.
        if (actor.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can modify any reservation");
        }
        ensureReservationExists(reservation);
        return updateReservation(reservation);
    }

    public Reservation modifyOwnReservation(User actor, Reservation reservation) {
        if (actor == null || actor.getId() == null) {
            throw new IllegalArgumentException("actor must not be null");
        }
        ensureReservationExists(reservation);

        Reservation existing = reservationRepository.findById(reservation.getId()).orElseThrow();
        // Owner check: the actor id must match the reservation client id.
        if (existing.getClient() == null || existing.getClient().getId() == null
                || !actor.getId().equals(existing.getClient().getId())) {
            throw new SecurityException("Users can only modify their own reservations");
        }
        return updateReservation(reservation);
    }

    public boolean cancelReservation(Long id) {
        Optional<Reservation> optional = reservationRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }
        Reservation reservation = optional.get();
        reservation.setStatus(Reservation.Status.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        notifyObservers(ReservationEventType.CANCELLED, saved);
        return true;
    }

    public boolean confirmReservation(Long id) {
        Optional<Reservation> optional = reservationRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }
        Reservation reservation = optional.get();
        reservation.setStatus(Reservation.Status.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);
        notifyObservers(ReservationEventType.CONFIRMED, saved);
        return true;
    }

    public boolean isRoomAvailable(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        return isRoomAvailable(roomId, startDate, endDate, null);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Optional<Reservation> getReservationByReference(String reference) {
        return reservationRepository.findByReference(reference);
    }

    public List<Reservation> getReservationsByClient(Long clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    public List<Reservation> getUpcomingReservationsByClient(Long clientId, LocalDateTime fromDate) {
        if (clientId == null || fromDate == null) {
            throw new IllegalArgumentException("clientId and fromDate must not be null");
        }

        return reservationRepository.findByClientId(clientId).stream()
            // Cancelled reservations are hidden from "upcoming" results.
                .filter(reservation -> reservation.getStatus() != Reservation.Status.CANCELLED)
                .filter(reservation -> reservation.getStartDate() != null && reservation.getStartDate().isAfter(fromDate))
                .sorted((left, right) -> left.getStartDate().compareTo(right.getStartDate()))
                .toList();
    }

    public List<Reservation> getReservationsByStatus(Reservation.Status status) {
        return reservationRepository.findByStatus(status);
    }

    public List<Reservation> getReservationsByRoom(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId must not be null");
        }

        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            if (reservation.getRoom() != null
                    && reservation.getRoom().getId() != null
                    && roomId.equals(reservation.getRoom().getId())) {
                result.add(reservation);
            }
        }
        return result;
    }

    public List<Reservation> getReservationsByRoomAndPeriod(Long roomId, LocalDateTime from, LocalDateTime to) {
        if (roomId == null || from == null || to == null) {
            throw new IllegalArgumentException("roomId, from and to must not be null");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }

        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            if (reservation.getRoom() == null || reservation.getRoom().getId() == null
                    || !roomId.equals(reservation.getRoom().getId())) {
                continue;
            }
            if (reservation.getStartDate() == null || reservation.getEndDate() == null) {
                continue;
            }
            if (from.isBefore(reservation.getEndDate()) && to.isAfter(reservation.getStartDate())) {
                result.add(reservation);
            }
        }
        return result;
    }

    public List<Reservation> getRoomDailySchedule(Long roomId, java.time.LocalDate date) {
        if (roomId == null || date == null) {
            throw new IllegalArgumentException("roomId and date must not be null");
        }

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        return getReservationsByRoomAndPeriod(roomId, from, to).stream()
                .filter(reservation -> reservation.getStatus() != Reservation.Status.CANCELLED)
                .sorted((left, right) -> left.getStartDate().compareTo(right.getStartDate()))
                .toList();
    }

    public List<Room> getAvailableRoomsNow(List<Room> rooms, LocalDateTime currentDateTime) {
        if (rooms == null || currentDateTime == null) {
            throw new IllegalArgumentException("rooms and currentDateTime must not be null");
        }

        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room == null || room.getId() == null) {
                continue;
            }
            boolean occupied = false;
            for (Reservation reservation : reservationRepository.findAll()) {
                if (reservation.getStatus() == Reservation.Status.CANCELLED) {
                    continue;
                }
                if (reservation.getRoom() == null || reservation.getRoom().getId() == null
                        || !room.getId().equals(reservation.getRoom().getId())) {
                    continue;
                }
                if (reservation.getStartDate() == null || reservation.getEndDate() == null) {
                    continue;
                }
                if (!currentDateTime.isBefore(reservation.getStartDate())
                        && currentDateTime.isBefore(reservation.getEndDate())) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public List<Room> getAvailableRoomsForPeriod(List<Room> rooms, LocalDateTime startDate, LocalDateTime endDate) {
        if (rooms == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("rooms, startDate and endDate must not be null");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room == null || room.getId() == null) {
                continue;
            }
            if (isRoomAvailable(room.getId(), startDate, endDate)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public boolean deleteReservation(Long id) {
        Optional<Reservation> existing = reservationRepository.findById(id);
        boolean deleted = reservationRepository.delete(id);
        if (deleted && existing.isPresent()) {
            notifyObservers(ReservationEventType.DELETED, existing.get());
        }
        return deleted;
    }

    public void registerObserver(ReservationObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer must not be null");
        }
        observers.add(observer);
    }

    public boolean unregisterObserver(ReservationObserver observer) {
        if (observer == null) {
            return false;
        }
        return observers.remove(observer);
    }

    public List<Reservation> getConflictingReservations(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        if (roomId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("roomId, startDate and endDate must not be null");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        List<Reservation> conflicts = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            if (reservation.getStatus() == Reservation.Status.CANCELLED) {
                continue;
            }
            if (reservation.getRoom() == null || reservation.getRoom().getId() == null
                    || !roomId.equals(reservation.getRoom().getId())) {
                continue;
            }
            if (reservation.getStartDate() == null || reservation.getEndDate() == null) {
                continue;
            }
            if (startDate.isBefore(reservation.getEndDate()) && endDate.isAfter(reservation.getStartDate())) {
                conflicts.add(reservation);
            }
        }
        return conflicts;
    }

    public long getRoomOccupancyDuration(Long roomId, LocalDateTime from, LocalDateTime to) {
        if (roomId == null || from == null || to == null) {
            throw new IllegalArgumentException("roomId, from and to must not be null");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }

        long totalMinutes = 0;
        for (Reservation reservation : getConflictingReservations(roomId, from, to)) {
            LocalDateTime actualStart = from.isAfter(reservation.getStartDate()) ? from : reservation.getStartDate();
            LocalDateTime actualEnd = to.isBefore(reservation.getEndDate()) ? to : reservation.getEndDate();
            totalMinutes += java.time.temporal.ChronoUnit.MINUTES.between(actualStart, actualEnd);
        }
        return totalMinutes;
    }

    public double getRoomOccupancyPercentage(Long roomId, LocalDateTime from, LocalDateTime to) {
        if (roomId == null || from == null || to == null) {
            throw new IllegalArgumentException("roomId, from and to must not be null");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }

        long totalMinutes = java.time.temporal.ChronoUnit.MINUTES.between(from, to);
        if (totalMinutes == 0) {
            return 0.0;
        }
        long occupiedMinutes = getRoomOccupancyDuration(roomId, from, to);
        return (double) occupiedMinutes / totalMinutes * 100;
    }

    public boolean hasConflicts(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        return !getConflictingReservations(roomId, startDate, endDate).isEmpty();
    }

    private boolean isRoomAvailable(Long roomId, LocalDateTime startDate, LocalDateTime endDate, Long excludedReservationId) {
        if (roomId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("roomId, startDate and endDate must not be null");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        for (Reservation existing : reservationRepository.findAll()) {
            if (excludedReservationId != null && excludedReservationId.equals(existing.getId())) {
                continue;
            }
            if (existing.getStatus() == Reservation.Status.CANCELLED) {
                continue;
            }
            if (existing.getRoom() == null || existing.getRoom().getId() == null
                    || !roomId.equals(existing.getRoom().getId())) {
                continue;
            }
            if (existing.getStartDate() == null || existing.getEndDate() == null) {
                continue;
            }
            if (startDate.isBefore(existing.getEndDate()) && endDate.isAfter(existing.getStartDate())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSchedulableData(Reservation reservation) {
        return reservation.getRoom() != null
                && reservation.getRoom().getId() != null
                && reservation.getStartDate() != null
                && reservation.getEndDate() != null;
    }

    private void validateReservation(Reservation reservation) {
        if (reservation.getStartDate() == null ^ reservation.getEndDate() == null) {
            throw new IllegalArgumentException("startDate and endDate must both be set or both be null");
        }
        if (reservation.getStartDate() != null && !reservation.getEndDate().isAfter(reservation.getStartDate())) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
        if (reservation.getParticipantCount() < 0) {
            throw new IllegalArgumentException("participantCount must be >= 0");
        }
        if (reservation.getRoom() != null
                && reservation.getParticipantCount() > 0
                && reservation.getParticipantCount() > reservation.getRoom().getCapacity()) {
            throw new IllegalArgumentException("participantCount exceeds room capacity");
        }
    }

    private String generateReference(Reservation reservation) {
        long sequence;
        if (reservation.getId() != null) {
            sequence = reservation.getId();
        } else {
            sequence = reservationRepository.count() + 1;
        }
        return String.format("RES-%03d", sequence);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void notifyObservers(ReservationEventType eventType, Reservation reservation) {
        for (ReservationObserver observer : observers) {
            observer.onReservationEvent(eventType, reservation);
        }
    }

    private void ensureReservationExists(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation id must not be null for update");
        }
        if (reservationRepository.findById(reservation.getId()).isEmpty()) {
            throw new IllegalArgumentException("Reservation not found");
        }
    }
}
