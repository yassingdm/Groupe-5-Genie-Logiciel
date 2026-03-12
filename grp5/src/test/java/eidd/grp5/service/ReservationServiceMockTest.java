package eidd.grp5.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import eidd.grp5.model.Reservation;
import eidd.grp5.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceMockTest {

    @Mock
    private ReservationRepository repository;

    @InjectMocks
    private ReservationService service;

    /** Vérifie que createReservation appelle bien save() une seule fois. */
    @Test
    void shouldCallSaveOnceWhenCreatingReservation() {
        Reservation reservation = new Reservation();
        when(repository.save(any())).thenReturn(reservation);

        service.createReservation(reservation);

        verify(repository, times(1)).save(reservation);
    }

    /** Vérifie que cancelReservation retourne false quand l'ID n'existe pas. */
    @Test
    void shouldReturnFalseWhenCancellingNonExistentReservation() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        boolean result = service.cancelReservation(99L);

        assertFalse(result);
        verify(repository, times(0)).save(any());
    }

    /** Vérifie que deleteReservation délègue bien au repository. */
    @Test
    void shouldDelegateDeleteToRepository() {
        when(repository.delete(1L)).thenReturn(true);

        boolean result = service.deleteReservation(1L);

        assertFalse(!result);
        verify(repository, times(1)).delete(1L);
    }
}
