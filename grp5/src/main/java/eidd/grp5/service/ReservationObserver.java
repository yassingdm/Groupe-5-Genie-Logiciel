package eidd.grp5.service;

import eidd.grp5.model.Reservation;

@FunctionalInterface
public interface ReservationObserver {
    // Observer pattern: listeners receive reservation events without tight coupling to service internals.
    void onReservationEvent(ReservationEventType eventType, Reservation reservation);
}
