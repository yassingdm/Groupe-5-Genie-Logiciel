package eidd.grp5.repository;

import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import eidd.grp5.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ReservationRepository implements Repository<Reservation> {

    private final List<Reservation> reservations = new ArrayList<>();
    
    @Override
    public Reservation save(Reservation entity) {
        Reservation safeEntity = copyReservation(entity);
        if (entity.getId() == null) {
            // New reservation: assign an id and store it.
            long newId = reservations.size() + 1L;
            entity.setId(newId);
            safeEntity.setId(newId);
            reservations.add(safeEntity);
        } else {
            // Existing reservation: replace by id.
            for (int i = 0; i < reservations.size(); i++) {
                if (reservations.get(i).getId().equals(entity.getId())) {
                    reservations.set(i, safeEntity);
                    break;
                }
            }
        }
        return copyReservation(safeEntity);
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            result.add(copyReservation(reservation));
        }
        return result;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(this::copyReservation);
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
            .findFirst()
            .map(this::copyReservation);
    }

    private List<Reservation> filterReservations(Predicate<Reservation> predicate) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (predicate.test(reservation)) {
                result.add(copyReservation(reservation));
            }
        }
        return result;
    }

    private Reservation copyReservation(Reservation reservation) {
        Reservation copy = new Reservation();
        copy.setId(reservation.getId());
        copy.setReference(reservation.getReference());
        copy.setClient(copyUser(reservation.getClient()));
        copy.setRoom(copyRoom(reservation.getRoom()));
        copy.setStartDate(reservation.getStartDate());
        copy.setEndDate(reservation.getEndDate());
        copy.setCreationDate(reservation.getCreationDate());
        copy.setParticipantCount(reservation.getParticipantCount());
        copy.setPurpose(reservation.getPurpose());
        copy.setStatus(reservation.getStatus());
        return copy;
    }

    private User copyUser(User user) {
        if (user == null) {
            return null;
        }
        User copy = new User(user.getName(), user.getEmail());
        copy.setId(user.getId());
        copy.setRole(user.getRole());
        return copy;
    }

    private Room copyRoom(Room room) {
        if (room == null) {
            return null;
        }
        Room copy = new Room(0, room.getName(), room.getCapacity(), room.getDescription());
        copy.setId(room.getId());
        return copy;
    }
}
