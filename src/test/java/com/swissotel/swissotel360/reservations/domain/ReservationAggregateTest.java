package com.swissotel.swissotel360.reservations.domain;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.events.*;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidReservationTransitionException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotModifiableException;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservationAggregateTest {
    private static final Clock CLOCK=Clock.fixed(Instant.parse("2026-10-27T18:30:00Z"),ZoneOffset.UTC);
    private Reservation create(PaymentType type) {
        return Reservation.create(new ReservationCode("SW26-7H3KQ9XA"),new GuestId(1L),new RoomTypeId(2L),
                new StayPeriod(LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-13")),type,
                new Money(new BigDecimal("1140.00")),new UserId(3L),CLOCK);
    }
    @Test void deferredReservationStartsConfirmedAndPublishesBothCreationEvents() {
        var reservation=create(PaymentType.DEFERRED);
        assertEquals(ReservationStatus.CONFIRMED,reservation.status());
        assertTrue(reservation.domainEvents().stream().anyMatch(ReservationCreated.class::isInstance));
        assertTrue(reservation.domainEvents().stream().anyMatch(ReservationConfirmed.class::isInstance));
        assertTrue(reservation.isModifiable());
    }
    @Test void immediateReservationStartsPendingAndCanBeConfirmedIdempotentlyByUseCase() {
        var reservation=create(PaymentType.IMMEDIATE);
        assertEquals(ReservationStatus.PENDING,reservation.status());
        assertEquals(1,reservation.domainEvents().stream().filter(ReservationCreated.class::isInstance).count());
        reservation.confirm();
        assertEquals(ReservationStatus.CONFIRMED,reservation.status());
        assertThrows(InvalidReservationTransitionException.class,reservation::confirm);
    }
    @Test void cancelledReservationRecordsReasonTimeAndCannotBeModified() {
        var reservation=create(PaymentType.DEFERRED);
        reservation.cancel(CancellationReason.STAFF,CLOCK,"Guest requested cancellation");
        assertEquals(ReservationStatus.CANCELLED,reservation.status());
        assertEquals(Instant.now(CLOCK),reservation.cancelledAt());
        assertEquals(CancellationReason.STAFF,reservation.cancellationReason());
        assertFalse(reservation.isActive());
        assertThrows(ReservationNotModifiableException.class,()->reservation.reschedule(new RoomTypeId(4L),reservation.period(),reservation.totalAmount()));
        assertThrows(InvalidReservationTransitionException.class,reservation::complete);
    }
    @Test void confirmedPrepaidReservationCannotBeRescheduled() {
        var reservation=create(PaymentType.IMMEDIATE); reservation.confirm();
        assertFalse(reservation.isModifiable());
        assertThrows(ReservationNotModifiableException.class,()->reservation.reschedule(new RoomTypeId(4L),reservation.period(),reservation.totalAmount()));
    }
}
