package eidd.grp5.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        assertThrows(IllegalArgumentException.class, () -> service.updateReservation(reservation));
    }
}
