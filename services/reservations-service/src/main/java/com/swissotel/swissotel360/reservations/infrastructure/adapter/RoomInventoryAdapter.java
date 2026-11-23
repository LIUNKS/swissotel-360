package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort;
import com.swissotel.swissotel360.reservations.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import com.swissotel.swissotel360.reservations.infrastructure.http.RoomTypeCollaboratorSnapshot;
import com.swissotel.swissotel360.reservations.infrastructure.http.RoomsInternalClient;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Component
public class RoomInventoryAdapter implements RoomInventoryPort {
	private static final Pattern ISO_DATE = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");

	private final RoomsInternalClient rooms;

	public RoomInventoryAdapter(RoomsInternalClient rooms) {
		this.rooms = rooms;
	}

	@Override
	public Optional<RoomTypeSnapshot> findRoomType(UUID publicId) {
		return getOrEmpty(() -> rooms.getRoomTypeByPublicId(publicId)).map(RoomInventoryAdapter::map);
	}

	@Override
	public Optional<RoomTypeSnapshot> findRoomTypeById(RoomTypeId id) {
		return getOrEmpty(() -> rooms.getRoomTypeById(id.value())).map(RoomInventoryAdapter::map);
	}

	@Override
	public int sellableUnits(RoomTypeId id) {
		var response = rooms.getSellableUnits(id.value());
		return response == null ? 0 : response.sellableUnits();
	}

	@Override
	public PriceQuote quote(RoomTypeId id, StayPeriod period) {
		try {
			var snapshot = rooms.quote(id.value(), period.checkIn(), period.checkOut());
			var breakdown = snapshot.breakdown() == null
					? List.<NightPrice>of()
					: snapshot.breakdown().stream().map(n -> new NightPrice(n.date(), new Money(n.price()))).toList();
			return new PriceQuote(new Money(snapshot.totalAmount()), breakdown);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().value() == 422) {
				throw new RateNotFoundException(extractMissingDate(ex.getResponseBodyAsString(), period.checkIn()));
			}
			throw ex;
		}
	}

	private static LocalDate extractMissingDate(String body, LocalDate fallback) {
		if (body != null && !body.isBlank()) {
			var matcher = ISO_DATE.matcher(body);
			if (matcher.find()) {
				try {
					return LocalDate.parse(matcher.group(1));
				} catch (DateTimeParseException ignored) {
					return fallback;
				}
			}
		}
		return fallback;
	}

	private static Optional<RoomTypeCollaboratorSnapshot> getOrEmpty(Supplier<RoomTypeCollaboratorSnapshot> call) {
		try {
			return Optional.ofNullable(call.get());
		} catch (HttpClientErrorException.NotFound ignored) {
			return Optional.empty();
		}
	}

	private static RoomTypeSnapshot map(RoomTypeCollaboratorSnapshot s) {
		return new RoomTypeSnapshot(new RoomTypeId(s.id()), s.publicId(), s.name(), s.capacity());
	}
}
