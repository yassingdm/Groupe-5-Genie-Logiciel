package eidd.grp5.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eidd.grp5.model.Reservation;
import eidd.grp5.repository.IReservationRepository; 

public class ReservationService {

    
    private final IReservationRepository reservationRepository;

    public ReservationService(IReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    
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
        return reservationRepository.save(reservation);
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

        return reservationRepository.save(reservation);
    }

    public boolean cancelReservation(Long id) {
        Optional<Reservation> optional = reservationRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }
        Reservation reservation = optional.get();
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);
        return true;
    }

    public boolean confirmReservation(Long id) {
        Optional<Reservation> optional = reservationRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }
        Reservation reservation = optional.get();
        reservation.setStatus(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);
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

    public boolean deleteReservation(Long id) {
        return reservationRepository.delete(id);
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
}
