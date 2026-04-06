package eidd.grp5.repository;

import java.util.List;
import java.util.Optional;
import eidd.grp5.model.Reservation;


public interface IReservationRepository extends Repository<Reservation> {
    
    List<Reservation> findByClientId(Long clientId);
    
    List<Reservation> findByStatus(Reservation.Status status);
    
    Optional<Reservation> findByReference(String reference);
}
