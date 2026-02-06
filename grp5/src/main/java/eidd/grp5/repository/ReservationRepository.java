package eidd.grp5.repository;

import eidd.grp5.model.Reservation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationRepository implements Repository<Reservation> {

    private List<Reservation> reservations = new ArrayList<>();
    
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
}
