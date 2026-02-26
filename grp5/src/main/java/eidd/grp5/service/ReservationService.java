package eidd.grp5.service;

import java.time.LocalDateTime;
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

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public boolean deleteReservation(Long id) {
        return reservationRepository.delete(id);
    }
}
