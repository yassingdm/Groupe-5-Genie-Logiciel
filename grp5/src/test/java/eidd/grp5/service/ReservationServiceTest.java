package eidd.grp5.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Reservation;
import eidd.grp5.repository.ReservationRepository;

class ReservationServiceTest {

    @Test
    void shouldSetDefaultsWhenCreatingReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();

        Reservation saved = service.createReservation(reservation);

        assertNotNull(saved.getCreationDate());
        assertEquals(Reservation.Status.PENDING, saved.getStatus());
        assertNotNull(saved.getId());
    }

    @Test
    void shouldThrowWhenUpdatingReservationWithoutId() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.updateReservation(reservation));
        assertNotNull(exception);
    }

    @Test
    void shouldKeepExistingCreationDateAndStatusWhenCreatingReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();

        LocalDateTime fixedDate = LocalDateTime.of(2026, 3, 19, 10, 30);
        reservation.setCreationDate(fixedDate);
        reservation.setStatus(Reservation.Status.CONFIRMED);

        Reservation saved = service.createReservation(reservation);

        assertEquals(fixedDate, saved.getCreationDate());
        assertEquals(Reservation.Status.CONFIRMED, saved.getStatus());
    }

    @Test
    void shouldCancelExistingReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();

        Reservation saved = service.createReservation(reservation);
        boolean cancelled = service.cancelReservation(saved.getId());

        assertTrue(cancelled);
        assertEquals(Reservation.Status.CANCELLED,
                service.getReservationById(saved.getId()).orElseThrow().getStatus());
    }
}
