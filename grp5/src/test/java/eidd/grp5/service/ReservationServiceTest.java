package eidd.grp5.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import eidd.grp5.model.User;
import eidd.grp5.repository.IReservationRepository;
import eidd.grp5.repository.ReservationRepository;

class ReservationServiceTest {

    
    private ReservationService service;
    private Room defaultRoom;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        
        IReservationRepository repository = new ReservationRepository();
        service = new ReservationService(repository);
        
        defaultRoom = new Room(1, "A101", 10, "Salle test");
        start = LocalDateTime.of(2026, 4, 10, 10, 0);
        end = LocalDateTime.of(2026, 4, 10, 11, 0);
    }

    @Test
    void shouldSetDefaultsWhenCreatingReservation() {
        Reservation reservation = new Reservation(defaultRoom, start, end);
        Reservation saved = service.createReservation(reservation);

        assertNotNull(saved.getCreationDate());
        assertEquals(Reservation.Status.PENDING, saved.getStatus());
        assertNotNull(saved.getId());
        assertTrue(saved.getReference().startsWith("RES-"));
    }

    @Test
    void shouldThrowWhenUpdatingReservationWithoutId() {
        Reservation reservation = new Reservation(defaultRoom, start, end);
        assertThrows(IllegalArgumentException.class, () -> service.updateReservation(reservation));
    }

    @Test
    void shouldDetectRoomUnavailabilityAndRejectOverlap() {
        Reservation first = new Reservation(defaultRoom, start, end);
        service.createReservation(first);

        Reservation overlap = new Reservation(defaultRoom, start.plusMinutes(30), end.plusMinutes(30));
        assertThrows(IllegalStateException.class, () -> service.createReservation(overlap));
    }

    @Test
    void shouldThrowWhenParticipantCountIsNegative() {
        Reservation reservation = new Reservation(defaultRoom, start, end);
        assertThrows(IllegalArgumentException.class, () -> reservation.setParticipantCount(-1));
    }

    @Test
    void shouldThrowWhenEndDateIsBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Reservation(defaultRoom, end, start)
        );
    }

    @Test
    void shouldCalculateRoomOccupancyPercentage() {
        Reservation res = new Reservation(defaultRoom, start, end); 
        service.createReservation(res);

        double percentage = service.getRoomOccupancyPercentage(
                defaultRoom.getId(),
                start,
                start.plusHours(4));

        assertEquals(25.0, percentage, 0.01);
    }

    @Test
    void shouldGetReservationsByClient() {
        User alice = new User("Alice", "alice@mail.com");
        alice.setId(1L);

        Reservation res = new Reservation(defaultRoom, start, end);
        res.setClient(alice);
        service.createReservation(res);

        List<Reservation> results = service.getReservationsByClient(1L);
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getClient().getName());
    }

    @Test
    void shouldCancelReservation() {
        Reservation res = new Reservation(defaultRoom, start, end);
        Reservation saved = service.createReservation(res);

        boolean success = service.cancelReservation(saved.getId());
        
        assertTrue(success);
        assertEquals(Reservation.Status.CANCELLED, service.getReservationById(saved.getId()).get().getStatus());
    }

    @Test
    void shouldIgnoreCancelledReservationsInAvailabilityCheck() {
        Reservation res = new Reservation(defaultRoom, start, end);
        Reservation saved = service.createReservation(res);
        service.cancelReservation(saved.getId());

        assertTrue(service.isRoomAvailable(defaultRoom.getId(), start, end));
    }
}