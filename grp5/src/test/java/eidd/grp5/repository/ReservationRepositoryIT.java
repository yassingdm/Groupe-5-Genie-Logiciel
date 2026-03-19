package eidd.grp5.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Reservation;

class ReservationRepositoryIT {

    private final ReservationRepository repository = new ReservationRepository();

    /** Round-trip basique : save → findById retrouve la même entité. */
    @Test
    void shouldSaveAndFindReservationById() {
        Reservation reservation = new Reservation();
        reservation.setReference("RES-001");

        Reservation saved = repository.save(reservation);
        Optional<Reservation> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("RES-001", found.get().getReference());
    }

    /** Fichier inexistant : findById sur un ID absent retourne vide. */
    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        Optional<Reservation> found = repository.findById(999L);

        assertFalse(found.isPresent());
    }

    /** Cas limite : liste vide et caractères spéciaux dans la référence. */
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
}
