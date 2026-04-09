package eidd.grp5.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import eidd.grp5.model.User;
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
        assertEquals("RES-001", saved.getReference());
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
        reservation.setReference("CUSTOM-123");

        Reservation saved = service.createReservation(reservation);

        assertEquals(fixedDate, saved.getCreationDate());
        assertEquals(Reservation.Status.CONFIRMED, saved.getStatus());
        assertEquals("CUSTOM-123", saved.getReference());
    }

    @Test
    void shouldGenerateReferenceWhenUpdatingReservationWithoutOne() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();

        Reservation saved = service.createReservation(reservation);
        saved.setReference(null);

        Reservation updated = service.updateReservation(saved);

        assertEquals("RES-001", updated.getReference());
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

    @Test
    void shouldConfirmExistingReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();

        Reservation saved = service.createReservation(reservation);
        boolean confirmed = service.confirmReservation(saved.getId());

        assertTrue(confirmed);
        assertEquals(Reservation.Status.CONFIRMED,
                service.getReservationById(saved.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldReturnFalseWhenConfirmingUnknownReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        boolean confirmed = service.confirmReservation(999L);

        assertFalse(confirmed);
    }

    @Test
    void shouldDetectRoomUnavailabilityAndRejectOverlappingReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Salle réunion");

        Reservation existing = new Reservation();
        existing.setRoom(room);
        existing.setStartDate(LocalDateTime.of(2026, 3, 20, 10, 0));
        existing.setEndDate(LocalDateTime.of(2026, 3, 20, 11, 0));
        service.createReservation(existing);

        boolean available = service.isRoomAvailable(
                1L,
                LocalDateTime.of(2026, 3, 20, 10, 30),
                LocalDateTime.of(2026, 3, 20, 11, 30));

        assertFalse(available);

        Reservation overlap = new Reservation();
        overlap.setRoom(room);
        overlap.setStartDate(LocalDateTime.of(2026, 3, 20, 10, 30));
        overlap.setEndDate(LocalDateTime.of(2026, 3, 20, 11, 30));

        IllegalStateException overlapException = assertThrows(IllegalStateException.class,
            () -> service.createReservation(overlap));
        assertNotNull(overlapException);
    }

    @Test
    void shouldAllowSlotAfterCancellationAndValidateCapacity() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(2, "B202", 2, "Petite salle");

        Reservation first = new Reservation();
        first.setRoom(room);
        first.setStartDate(LocalDateTime.of(2026, 3, 21, 9, 0));
        first.setEndDate(LocalDateTime.of(2026, 3, 21, 10, 0));
        Reservation saved = service.createReservation(first);
        service.cancelReservation(saved.getId());

        Reservation second = new Reservation();
        second.setRoom(room);
        second.setStartDate(LocalDateTime.of(2026, 3, 21, 9, 15));
        second.setEndDate(LocalDateTime.of(2026, 3, 21, 9, 45));

        Reservation created = service.createReservation(second);
        assertNotNull(created.getId());

        Reservation tooManyParticipants = new Reservation();
        tooManyParticipants.setRoom(room);
        tooManyParticipants.setParticipantCount(3);

        IllegalArgumentException capacityException = assertThrows(IllegalArgumentException.class,
            () -> service.createReservation(tooManyParticipants));
        assertNotNull(capacityException);
    }

    @Test
    void shouldThrowWhenCheckingAvailabilityWithInvalidDates() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        IllegalArgumentException invalidDateException = assertThrows(IllegalArgumentException.class, () -> service.isRoomAvailable(
                1L,
                LocalDateTime.of(2026, 3, 22, 14, 0),
                LocalDateTime.of(2026, 3, 22, 13, 0)));
        assertNotNull(invalidDateException);
    }

    @Test
    void shouldGetReservationsByRoom() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        Room roomA = new Room(10, "A", 10, "Salle A");
        Room roomB = new Room(20, "B", 10, "Salle B");

        Reservation resA1 = new Reservation();
        resA1.setRoom(roomA);
        resA1.setStartDate(LocalDateTime.of(2026, 3, 23, 9, 0));
        resA1.setEndDate(LocalDateTime.of(2026, 3, 23, 10, 0));
        service.createReservation(resA1);

        Reservation resA2 = new Reservation();
        resA2.setRoom(roomA);
        resA2.setStartDate(LocalDateTime.of(2026, 3, 23, 11, 0));
        resA2.setEndDate(LocalDateTime.of(2026, 3, 23, 12, 0));
        service.createReservation(resA2);

        Reservation resB = new Reservation();
        resB.setRoom(roomB);
        resB.setStartDate(LocalDateTime.of(2026, 3, 23, 9, 0));
        resB.setEndDate(LocalDateTime.of(2026, 3, 23, 10, 0));
        service.createReservation(resB);

        List<Reservation> roomAReservations = service.getReservationsByRoom(10L);

        assertEquals(2, roomAReservations.size());
    }

    @Test
    void shouldGetReservationsByRoomAndPeriod() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        Room room = new Room(30, "C", 10, "Salle C");

        Reservation inside = new Reservation();
        inside.setRoom(room);
        inside.setStartDate(LocalDateTime.of(2026, 3, 24, 10, 0));
        inside.setEndDate(LocalDateTime.of(2026, 3, 24, 11, 0));
        service.createReservation(inside);

        Reservation outside = new Reservation();
        outside.setRoom(room);
        outside.setStartDate(LocalDateTime.of(2026, 3, 24, 13, 0));
        outside.setEndDate(LocalDateTime.of(2026, 3, 24, 14, 0));
        service.createReservation(outside);

        List<Reservation> result = service.getReservationsByRoomAndPeriod(
                30L,
                LocalDateTime.of(2026, 3, 24, 9, 30),
                LocalDateTime.of(2026, 3, 24, 11, 30));

        assertEquals(1, result.size());
        assertEquals(inside.getId(), result.get(0).getId());
    }

    @Test
    void shouldGetReservationsByClient() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        User alice = new User("Alice", "alice@mail.com");
        alice.setId(1L);
        User bob = new User("Bob", "bob@mail.com");
        bob.setId(2L);

        Reservation first = new Reservation();
        first.setClient(alice);
        service.createReservation(first);

        Reservation second = new Reservation();
        second.setClient(alice);
        service.createReservation(second);

        Reservation third = new Reservation();
        third.setClient(bob);
        service.createReservation(third);

        List<Reservation> aliceReservations = service.getReservationsByClient(1L);

        assertEquals(2, aliceReservations.size());
    }

    @Test
    void shouldGetReservationsByStatus() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        Reservation pending = new Reservation();
        pending.setStatus(Reservation.Status.PENDING);
        service.createReservation(pending);

        Reservation confirmed = new Reservation();
        confirmed.setStatus(Reservation.Status.CONFIRMED);
        service.createReservation(confirmed);

        Reservation cancelled = new Reservation();
        cancelled.setStatus(Reservation.Status.CANCELLED);
        service.createReservation(cancelled);

        List<Reservation> confirmedReservations = service.getReservationsByStatus(Reservation.Status.CONFIRMED);

        assertEquals(1, confirmedReservations.size());
        assertEquals(Reservation.Status.CONFIRMED, confirmedReservations.get(0).getStatus());
    }

    @Test
    void shouldFindReservationByReference() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        Reservation reservation = new Reservation();
        reservation.setReference("RES-777");

        Reservation saved = service.createReservation(reservation);

        assertTrue(service.getReservationByReference("RES-777").isPresent());
        assertEquals(saved.getId(), service.getReservationByReference("RES-777").orElseThrow().getId());
    }

    @Test
    void shouldThrowWhenFilteringWithInvalidPeriod() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getReservationsByRoomAndPeriod(
                        1L,
                        LocalDateTime.of(2026, 3, 24, 12, 0),
                        LocalDateTime.of(2026, 3, 24, 12, 0)));

        assertNotNull(exception);
    }

    @Test
    void shouldDetectConflictingReservations() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation existing = new Reservation();
        existing.setRoom(room);
        existing.setStartDate(LocalDateTime.of(2026, 4, 10, 10, 0));
        existing.setEndDate(LocalDateTime.of(2026, 4, 10, 11, 0));
        service.createReservation(existing);

        List<Reservation> conflicts = service.getConflictingReservations(
                1L,
                LocalDateTime.of(2026, 4, 10, 10, 30),
                LocalDateTime.of(2026, 4, 10, 11, 30));

        assertEquals(1, conflicts.size());
        assertEquals(existing.getId(), conflicts.get(0).getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoConflicts() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation existing = new Reservation();
        existing.setRoom(room);
        existing.setStartDate(LocalDateTime.of(2026, 4, 10, 10, 0));
        existing.setEndDate(LocalDateTime.of(2026, 4, 10, 11, 0));
        service.createReservation(existing);

        List<Reservation> conflicts = service.getConflictingReservations(
                1L,
                LocalDateTime.of(2026, 4, 10, 13, 0),
                LocalDateTime.of(2026, 4, 10, 14, 0));

        assertTrue(conflicts.isEmpty());
    }

    @Test
    void shouldIgnoreCancelledReservationsInConflictDetection() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation existing = new Reservation();
        existing.setRoom(room);
        existing.setStartDate(LocalDateTime.of(2026, 4, 10, 10, 0));
        existing.setEndDate(LocalDateTime.of(2026, 4, 10, 11, 0));
        Reservation saved = service.createReservation(existing);
        service.cancelReservation(saved.getId());

        List<Reservation> conflicts = service.getConflictingReservations(
                1L,
                LocalDateTime.of(2026, 4, 10, 10, 30),
                LocalDateTime.of(2026, 4, 10, 11, 30));

        assertTrue(conflicts.isEmpty());
    }

    @Test
    void shouldCalculateRoomOccupancyDuration() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation first = new Reservation();
        first.setRoom(room);
        first.setStartDate(LocalDateTime.of(2026, 4, 10, 10, 0));
        first.setEndDate(LocalDateTime.of(2026, 4, 10, 11, 0));
        service.createReservation(first);

        Reservation second = new Reservation();
        second.setRoom(room);
        second.setStartDate(LocalDateTime.of(2026, 4, 10, 14, 0));
        second.setEndDate(LocalDateTime.of(2026, 4, 10, 15, 30));
        service.createReservation(second);

        long occupiedMinutes = service.getRoomOccupancyDuration(
                1L,
                LocalDateTime.of(2026, 4, 10, 9, 0),
                LocalDateTime.of(2026, 4, 10, 16, 0));

        assertEquals(150, occupiedMinutes);
    }

    @Test
    void shouldCalculateRoomOccupancyPercentage() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setStartDate(LocalDateTime.of(2026, 4, 10, 10, 0));
        reservation.setEndDate(LocalDateTime.of(2026, 4, 10, 12, 0));
        service.createReservation(reservation);

        double percentage = service.getRoomOccupancyPercentage(
                1L,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 14, 0));

        assertEquals(50.0, percentage, 0.01);
    }

    @Test
    void shouldDetectConflicts() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation existing = new Reservation();
        existing.setRoom(room);
        existing.setStartDate(LocalDateTime.of(2026, 4, 10, 10, 0));
        existing.setEndDate(LocalDateTime.of(2026, 4, 10, 11, 0));
        service.createReservation(existing);

        assertTrue(service.hasConflicts(1L, LocalDateTime.of(2026, 4, 10, 10, 30), LocalDateTime.of(2026, 4, 10, 11, 30)));
        assertFalse(service.hasConflicts(1L, LocalDateTime.of(2026, 4, 10, 12, 0), LocalDateTime.of(2026, 4, 10, 13, 0)));
    }

    @Test
    void shouldThrowWhenReservationHasPartialDates() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDateTime.of(2026, 4, 12, 10, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createReservation(reservation));
        assertNotNull(exception);
    }

    @Test
    void shouldThrowWhenParticipantCountIsNegative() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Reservation reservation = new Reservation();
        reservation.setParticipantCount(-1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createReservation(reservation));
        assertNotNull(exception);
    }

    @Test
    void shouldAllowUpdateWhenOverlappingSameReservationId() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Conference room");

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setStartDate(LocalDateTime.of(2026, 4, 13, 10, 0));
        reservation.setEndDate(LocalDateTime.of(2026, 4, 13, 11, 0));
        Reservation saved = service.createReservation(reservation);

        saved.setReference(null);
        Reservation updated = service.updateReservation(saved);

        assertEquals("RES-001", updated.getReference());
    }

    @Test
    void shouldThrowWhenFilteringByRoomWithNullId() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        assertThrows(IllegalArgumentException.class, () -> service.getReservationsByRoom(null));
    }

    @Test
    void shouldThrowWhenFilteringByRoomAndPeriodWithNullValues() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.getReservationsByRoomAndPeriod(1L, null, LocalDateTime.now()));
    }

    @Test
    void shouldThrowWhenCheckingAvailabilityWithNullRoomId() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.isRoomAvailable(null, LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
    }

    @Test
    void shouldAllowAdminToModifyAnyReservation() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room roomA = new Room(1, "A101", 10, "Salle A");
        Room roomB = new Room(2, "B101", 10, "Salle B");

        User owner = new User("Owner", "owner@mail.com");
        owner.setId(10L);

        Reservation reservation = new Reservation();
        reservation.setClient(owner);
        reservation.setRoom(roomA);
        reservation.setStartDate(LocalDateTime.of(2026, 5, 2, 9, 0));
        reservation.setEndDate(LocalDateTime.of(2026, 5, 2, 10, 0));
        Reservation saved = service.createReservation(reservation);

        User admin = new User("Admin", "admin@mail.com");
        admin.setId(1L);
        admin.setRole(User.Role.ADMIN);

        saved.setRoom(roomB);
        saved.setStartDate(LocalDateTime.of(2026, 5, 2, 11, 0));
        saved.setEndDate(LocalDateTime.of(2026, 5, 2, 12, 0));

        Reservation updated = service.modifyReservationAsAdmin(admin, saved);

        assertEquals(2L, updated.getRoom().getId());
        assertEquals(LocalDateTime.of(2026, 5, 2, 11, 0), updated.getStartDate());
    }

    @Test
    void shouldRejectNonAdminForAdminModification() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);

        Reservation reservation = service.createReservation(new Reservation());
        User customer = new User("Customer", "customer@mail.com");
        customer.setId(2L);
        customer.setRole(User.Role.CUSTOMER);

        assertThrows(SecurityException.class, () -> service.modifyReservationAsAdmin(customer, reservation));
    }

    @Test
    void shouldAllowOwnerToModifyOwnReservationAndRejectOthers() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room room = new Room(1, "A101", 10, "Salle A");

        User owner = new User("Owner", "owner@mail.com");
        owner.setId(100L);
        User otherUser = new User("Other", "other@mail.com");
        otherUser.setId(200L);

        Reservation reservation = new Reservation();
        reservation.setClient(owner);
        reservation.setRoom(room);
        reservation.setStartDate(LocalDateTime.of(2026, 5, 3, 10, 0));
        reservation.setEndDate(LocalDateTime.of(2026, 5, 3, 11, 0));
        Reservation saved = service.createReservation(reservation);

        saved.setEndDate(LocalDateTime.of(2026, 5, 3, 11, 30));
        Reservation updated = service.modifyOwnReservation(owner, saved);
        assertEquals(LocalDateTime.of(2026, 5, 3, 11, 30), updated.getEndDate());

        assertThrows(SecurityException.class, () -> service.modifyOwnReservation(otherUser, saved));
    }

    @Test
    void shouldReturnUpcomingReservationsSortedWithoutCancelled() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        User client = new User("Alice", "alice@mail.com");
        client.setId(1L);

        Reservation oldReservation = new Reservation();
        oldReservation.setClient(client);
        oldReservation.setStartDate(LocalDateTime.of(2026, 4, 1, 10, 0));
        oldReservation.setEndDate(LocalDateTime.of(2026, 4, 1, 11, 0));
        service.createReservation(oldReservation);

        Reservation next = new Reservation();
        next.setClient(client);
        next.setStartDate(LocalDateTime.of(2026, 6, 1, 10, 0));
        next.setEndDate(LocalDateTime.of(2026, 6, 1, 11, 0));
        service.createReservation(next);

        Reservation laterCancelled = new Reservation();
        laterCancelled.setClient(client);
        laterCancelled.setStatus(Reservation.Status.CANCELLED);
        laterCancelled.setStartDate(LocalDateTime.of(2026, 7, 1, 10, 0));
        laterCancelled.setEndDate(LocalDateTime.of(2026, 7, 1, 11, 0));
        service.createReservation(laterCancelled);

        Reservation latest = new Reservation();
        latest.setClient(client);
        latest.setStartDate(LocalDateTime.of(2026, 6, 2, 10, 0));
        latest.setEndDate(LocalDateTime.of(2026, 6, 2, 11, 0));
        service.createReservation(latest);

        List<Reservation> result = service.getUpcomingReservationsByClient(1L, LocalDateTime.of(2026, 5, 1, 0, 0));

        assertEquals(2, result.size());
        assertEquals(next.getId(), result.get(0).getId());
        assertEquals(latest.getId(), result.get(1).getId());
    }

    @Test
    void shouldReturnRoomDailyScheduleAndAvailableRooms() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        Room roomA = new Room(1, "A", 10, "Salle A");
        Room roomB = new Room(2, "B", 10, "Salle B");

        Reservation reservation = new Reservation();
        reservation.setRoom(roomA);
        reservation.setStartDate(LocalDateTime.of(2026, 6, 10, 10, 0));
        reservation.setEndDate(LocalDateTime.of(2026, 6, 10, 11, 0));
        service.createReservation(reservation);

        List<Reservation> schedule = service.getRoomDailySchedule(1L, LocalDate.of(2026, 6, 10));
        assertEquals(1, schedule.size());

        List<Room> nowAvailable = service.getAvailableRoomsNow(
                List.of(roomA, roomB),
                LocalDateTime.of(2026, 6, 10, 10, 30));
        assertEquals(1, nowAvailable.size());
        assertEquals(2L, nowAvailable.get(0).getId());

        List<Room> periodAvailable = service.getAvailableRoomsForPeriod(
                List.of(roomA, roomB),
                LocalDateTime.of(2026, 6, 10, 10, 15),
                LocalDateTime.of(2026, 6, 10, 10, 45));
        assertEquals(1, periodAvailable.size());
        assertEquals(2L, periodAvailable.get(0).getId());
    }

    @Test
    void shouldNotifyObserverOnReservationLifecycleEvents() {
        ReservationRepository repository = new ReservationRepository();
        ReservationService service = new ReservationService(repository);
        List<String> events = new ArrayList<>();

        ReservationObserver observer = (eventType, reservation) ->
                events.add(eventType.name() + "-" + reservation.getId());
        service.registerObserver(observer);

        Reservation reservation = new Reservation();
        Reservation created = service.createReservation(reservation);
        service.confirmReservation(created.getId());
        service.cancelReservation(created.getId());
        service.deleteReservation(created.getId());

        assertEquals(4, events.size());
        assertTrue(events.get(0).startsWith("CREATED-"));
        assertTrue(events.get(1).startsWith("CONFIRMED-"));
        assertTrue(events.get(2).startsWith("CANCELLED-"));
        assertTrue(events.get(3).startsWith("DELETED-"));
        assertTrue(service.unregisterObserver(observer));
    }
}
