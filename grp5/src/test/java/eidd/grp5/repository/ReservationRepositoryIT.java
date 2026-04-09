package eidd.grp5.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Reservation;
import eidd.grp5.model.User;

class ReservationRepositoryIT {

    private final ReservationRepository repository = new ReservationRepository();

    /** Basic round trip: save then findById returns the same entity. */
    @Test
    void shouldSaveAndFindReservationById() {
        Reservation reservation = new Reservation();
        reservation.setReference("RES-001");

        Reservation saved = repository.save(reservation);
        Optional<Reservation> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("RES-001", found.get().getReference());
    }

    /** Missing id case: findById returns empty for unknown id. */
    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        Optional<Reservation> found = repository.findById(999L);

        assertFalse(found.isPresent());
    }

    /** Edge case: empty list and special characters in reference. */
    @Test
    void shouldHandleEmptyListAndSpecialCharactersInReference() {
        List<Reservation> empty = repository.findAll();
        assertTrue(empty.isEmpty());

        Reservation reservation = new Reservation();
        reservation.setReference("RES @#$%");
        repository.save(reservation);

        Optional<Reservation> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("RES @#$%", found.get().getReference());
    }

    @Test
    void shouldReturnFalseWhenDeletingUnknownId() {
        boolean deleted = repository.delete(12345L);

        assertFalse(deleted);
    }

    @Test
    void shouldNotInsertWhenUpdatingUnknownId() {
        Reservation reservation = new Reservation();
        reservation.setId(999L);
        reservation.setReference("UNKNOWN");

        repository.save(reservation);

        assertEquals(0L, repository.count());
        assertFalse(repository.findById(999L).isPresent());
    }

    @Test
    void shouldReturnDefensiveCopyInFindAll() {
        Reservation reservation = new Reservation();
        reservation.setReference("RES-DEF");
        repository.save(reservation);

        List<Reservation> snapshot = repository.findAll();
        snapshot.clear();

        assertEquals(1L, repository.count());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldFindReservationsByClientId() {
        User alice = new User("Alice", "alice@mail.com");
        alice.setId(1L);
        User bob = new User("Bob", "bob@mail.com");
        bob.setId(2L);

        Reservation first = new Reservation();
        first.setClient(alice);
        repository.save(first);

        Reservation second = new Reservation();
        second.setClient(alice);
        repository.save(second);

        Reservation third = new Reservation();
        third.setClient(bob);
        repository.save(third);

        List<Reservation> result = repository.findByClientId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindReservationsByStatus() {
        Reservation pending = new Reservation();
        pending.setStatus(Reservation.Status.PENDING);
        repository.save(pending);

        Reservation confirmed = new Reservation();
        confirmed.setStatus(Reservation.Status.CONFIRMED);
        repository.save(confirmed);

        List<Reservation> result = repository.findByStatus(Reservation.Status.CONFIRMED);

        assertEquals(1, result.size());
        assertEquals(Reservation.Status.CONFIRMED, result.get(0).getStatus());
    }

    @Test
    void shouldFindReservationByReference() {
        Reservation reservation = new Reservation();
        reservation.setReference("RES-ABC");
        repository.save(reservation);

        Optional<Reservation> found = repository.findByReference("RES-ABC");

        assertTrue(found.isPresent());
        assertEquals("RES-ABC", found.get().getReference());
    }
}
