package com.swissotel.swissotel360.rooms.application.queryservices;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.swissotel.swissotel360.rooms.domain.model.queries.AvailabilitySearch;
import com.swissotel.swissotel360.rooms.domain.model.queries.QuoteRoomTypeQuery;

public interface AvailabilityQueryService {
	List<AvailabilityResult> search(AvailabilitySearch query);

	int countAvailable(QuoteRoomTypeQuery query);

	PriceQuote quote(QuoteRoomTypeQuery query);
}
