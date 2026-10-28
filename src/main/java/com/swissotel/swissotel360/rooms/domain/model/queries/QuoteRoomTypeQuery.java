package com.swissotel.swissotel360.rooms.domain.model.queries;
import java.time.LocalDate;
import java.util.UUID;
public record QuoteRoomTypeQuery(UUID roomTypePublicId,LocalDate checkIn,LocalDate checkOut) {
    public QuoteRoomTypeQuery { if (roomTypePublicId == null) throw new IllegalArgumentException("roomTypePublicId is required"); AvailabilitySearch.validatePeriod(checkIn,checkOut); }
}
