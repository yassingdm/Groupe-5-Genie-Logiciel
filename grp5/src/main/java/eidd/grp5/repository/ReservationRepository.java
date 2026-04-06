package eidd.grp5.repository;

import eidd.grp5.model.Reservation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ReservationRepository implements IReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    
    @Override
    public Reservation save(Reservation entity) {
        if (entity.getId() == null) {
            // Nouvelle réservation
            entity.setId((long) (reservations.size() + 1));
            reservations.add(entity);
        } else {
            // Mise à jour d'une réservation existante
            for (int i = 0; i < reservations.size(); i++) {
                if (reservations.get(i).getId().equals(entity.getId())) {
                    reservations.set(i, entity);
                    break;
                }
            }
        }
        return entity;
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean delete(Long id) {
        return reservations.removeIf(r -> r.getId().equals(id));
    }

    @Override
    public long count() {
        return reservations.size();
    }

    public List<Reservation> findByClientId(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId must not be null");
        }

        return filterReservations(reservation -> reservation.getClient() != null
                && reservation.getClient().getId() != null
                && clientId.equals(reservation.getClient().getId()));
    }

    public List<Reservation> findByStatus(Reservation.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        return filterReservations(reservation -> status.equals(reservation.getStatus()));
    }

    public Optional<Reservation> findByReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("reference must not be blank");
        }

        return reservations.stream()
                .filter(reservation -> reference.equals(reservation.getReference()))
                .findFirst();
    }

    private List<Reservation> filterReservations(Predicate<Reservation> predicate) {
        return reservations.stream()
                .filter(predicate)
                .toList();
    }
}
