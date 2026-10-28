package com.swissotel.swissotel360.reservations.domain.events;

import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
public record ReservationCreated(ReservationId id, ReservationCode code, GuestId guestId, RoomTypeId roomTypeId, StayPeriod period, PaymentType paymentType, Money total, ReservationStatus status) { }
