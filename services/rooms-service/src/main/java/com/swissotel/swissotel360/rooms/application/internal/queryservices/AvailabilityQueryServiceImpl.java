package com.swissotel.swissotel360.rooms.application.internal.queryservices;

import com.swissotel.swissotel360.rooms.application.queryservices.*;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.repositories.*;
import com.swissotel.swissotel360.rooms.domain.model.queries.AvailabilitySearch;
import com.swissotel.swissotel360.rooms.domain.model.queries.QuoteRoomTypeQuery;
import com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl.ReservationsAvailabilityService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AvailabilityQueryServiceImpl implements AvailabilityQueryService {
	private final RoomTypeRepository roomTypes;
	private final RoomRepository rooms;
	private final RateRepository rates;
	private final ReservationsAvailabilityService reservations;

	public AvailabilityQueryServiceImpl(RoomTypeRepository roomTypes, RoomRepository rooms, RateRepository rates,
			ReservationsAvailabilityService reservations) {
		this.roomTypes = roomTypes;
		this.rooms = rooms;
		this.rates = rates;
		this.reservations = reservations;
	}

	public List<AvailabilityResult> search(AvailabilitySearch query) {
		var checkIn = query.checkIn();
		var checkOut = query.checkOut();
		var guests = query.guests();
		return roomTypes.findAll().stream().filter(t -> guests == null || t.capacity() >= guests).map(t -> {
			var quoteQuery = new QuoteRoomTypeQuery(t.publicId(), checkIn, checkOut);
			var quote = quote(quoteQuery);
			return new AvailabilityResult(t.publicId(), t.name(), t.capacity(),
					countAvailable(t.id(), checkIn, checkOut),
					quote.totalAmount().divide(BigDecimal.valueOf(quote.nights()), 2, RoundingMode.HALF_UP),
					quote.totalAmount());
		}).toList();
	}

	public int countAvailable(QuoteRoomTypeQuery query) {
		var id = query.roomTypePublicId();
		var type = roomTypes.findByPublicId(id).orElseThrow(() -> new RoomTypeNotFoundException(id));
		return countAvailable(type.id(), query.checkIn(), query.checkOut());
	}

	private int countAvailable(Long typeId, LocalDate from, LocalDate to) {
		long active = rooms.countByRoomTypeIdAndStatusNot(typeId,
				com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus.OUT_OF_SERVICE);
		long reserved = reservations.countOverlappingActiveReservations(typeId, from, to);
		return (int) Math.max(0, active - reserved);
	}

	public PriceQuote quote(QuoteRoomTypeQuery query) {
		var id = query.roomTypePublicId();
		var checkIn = query.checkIn();
		var checkOut = query.checkOut();
		var type = roomTypes.findByPublicId(id).orElseThrow(() -> new RoomTypeNotFoundException(id));
		var nights = new java.util.ArrayList<PriceQuote.NightPrice>();
		var total = BigDecimal.ZERO;
		for (var date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
			var night = date;
			var price = rates.findEffective(type.id(), night).orElseThrow(() -> new RateNotFoundException(night))
					.price();
			nights.add(new PriceQuote.NightPrice(night, price));
			total = total.add(price);
		}
		return new PriceQuote(id, checkIn, checkOut, nights.size(), total, nights);
	}
}
