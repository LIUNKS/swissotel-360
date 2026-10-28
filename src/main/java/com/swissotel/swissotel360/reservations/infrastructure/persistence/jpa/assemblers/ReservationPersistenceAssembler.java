package com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.assemblers;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;

public final class ReservationPersistenceAssembler {
    private ReservationPersistenceAssembler() { }
    public static Reservation toDomain(ReservationPersistenceEntity e) {
        return Reservation.restore(new ReservationId(e.getPublicId()),new ReservationCode(e.getReservationCode()),
                new GuestId(e.getGuestId()),new RoomTypeId(e.getRoomTypeId()),new StayPeriod(e.getCheckInDate(),e.getCheckOutDate()),
                ReservationStatus.fromCode(e.getStatus()),PaymentType.fromCode(e.getPaymentType()),new Money(e.getTotalAmount()),
                new UserId(e.getCreatedBy()),e.getCreatedAt(),e.getCancelledAt(),e.getCancellationReason()==null?null:CancellationReason.fromCode(e.getCancellationReason()),e.getCancellationNote());
    }
    public static ReservationPersistenceEntity toEntity(Reservation a) { return toEntity(a,new ReservationPersistenceEntity()); }
    public static ReservationPersistenceEntity toEntity(Reservation a,ReservationPersistenceEntity e) {
        e.setPublicId(a.id().value()); e.setReservationCode(a.code().value());
        e.setGuestId(a.guestId().value()); e.setRoomTypeId(a.roomTypeId().value()); e.setCheckInDate(a.period().checkIn());
        e.setCheckOutDate(a.period().checkOut()); e.setStatus(a.status().code()); e.setPaymentType(a.paymentType().code());
        e.setTotalAmount(a.totalAmount().amount()); e.setCreatedBy(a.createdBy().value()); e.setCreatedAt(a.createdAt());
        e.setCancelledAt(a.cancelledAt()); e.setCancellationReason(a.cancellationReason()==null?null:a.cancellationReason().code()); e.setCancellationNote(a.cancellationNote()); return e;
    }
}
