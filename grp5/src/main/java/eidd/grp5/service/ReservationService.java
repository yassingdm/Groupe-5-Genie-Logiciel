package eidd.grp5.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eidd.grp5.model.Reservation;
import eidd.grp5.repository.ReservationRepository;

public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Crée une nouvelle réservation avec les informations minimales.
     */
    public Reservation createReservation(Reservation reservation) {
        validateReservation(reservation);

        if (hasSchedulableData(reservation)
                && !isRoomAvailable(reservation.getRoom().getId(), reservation.getStartDate(), reservation.getEndDate(), null)) {
            throw new IllegalStateException("Room is not available for the requested time slot");
        }

        // valeurs par défaut basiques
        if (reservation.getCreationDate() == null) {
            reservation.setCreationDate(LocalDateTime.now());
        }
        if (reservation.getStatus() == null) {
            reservation.setStatus(Reservation.Status.PENDING);
        }
        return reservationRepository.save(reservation);
    }

    /**
     * Met à jour une réservation existante.
     */
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

        return reservationRepository.save(reservation);
    }

    /**
     * Annule une réservation en changeant son statut.
     */
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
}
