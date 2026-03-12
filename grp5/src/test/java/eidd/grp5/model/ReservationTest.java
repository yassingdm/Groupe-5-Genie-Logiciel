package eidd.grp5.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void shouldCreateEmptyReservationObject() {
        Reservation reservation = new Reservation();

        assertNotNull(reservation);
        assertNull(reservation.getId());
        assertNull(reservation.getStatus());
    }

    @Test
    void shouldSetReservationFieldsInNominalCase() {
        Reservation reservation = new Reservation();
        User user = new User("Client", "client@mail.com");
        Room room = new Room(1, "Salle 1", 5, "Desc");
        LocalDateTime start = LocalDateTime.of(2026, 3, 12, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 12, 12, 0);

        reservation.setId(1L);
        reservation.setReference("RES-001");
        reservation.setClient(user);
        reservation.setRoom(room);
        reservation.setStartDate(start);
        reservation.setEndDate(end);
        reservation.setParticipantCount(4);
        reservation.setPurpose("Atelier");
        reservation.setStatus(Reservation.Status.CONFIRMED);

        assertEquals(1L, reservation.getId());
        assertEquals("RES-001", reservation.getReference());
        assertEquals(user, reservation.getClient());
        assertEquals(room, reservation.getRoom());
        assertEquals(start, reservation.getStartDate());
        assertEquals(end, reservation.getEndDate());
        assertEquals(4, reservation.getParticipantCount());
        assertEquals("Atelier", reservation.getPurpose());
        assertEquals(Reservation.Status.CONFIRMED, reservation.getStatus());
    }

    @Test
    void shouldAcceptNullReferenceAsEdgeCase() {
        Reservation reservation = new Reservation();
        reservation.setReference(null);

        assertNull(reservation.getReference());
    }
}
