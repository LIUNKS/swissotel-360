package com.swissotel.swissotel360.rooms.interfaces.rest.transform;

import com.swissotel.swissotel360.rooms.domain.model.queries.AvailabilitySearch;
import com.swissotel.swissotel360.rooms.domain.model.queries.QuoteRoomTypeQuery;
import java.time.LocalDate;
import java.util.UUID;

public final class AvailabilityQueryAssembler {
	private AvailabilityQueryAssembler() {
	}

	public static AvailabilitySearch toSearch(LocalDate checkIn, LocalDate checkOut, Integer guests) {
		return new AvailabilitySearch(checkIn, checkOut, guests);
	}

	public static QuoteRoomTypeQuery toQuote(UUID roomTypePublicId, LocalDate checkIn, LocalDate checkOut) {
		return new QuoteRoomTypeQuery(roomTypePublicId, checkIn, checkOut);
	}
}
