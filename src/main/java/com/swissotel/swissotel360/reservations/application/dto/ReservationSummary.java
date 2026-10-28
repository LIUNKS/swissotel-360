package com.swissotel.swissotel360.reservations.application.dto;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import java.util.UUID;
public record ReservationSummary(Reservation reservation, UUID guestPublicId, String guestName, UUID roomTypePublicId, String roomTypeName) { }
