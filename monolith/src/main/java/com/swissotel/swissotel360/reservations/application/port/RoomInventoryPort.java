package com.swissotel.swissotel360.reservations.application.port;

import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomInventoryPort {
	Optional<RoomTypeSnapshot> findRoomType(UUID roomTypePublicId);

	Optional<RoomTypeSnapshot> findRoomTypeById(RoomTypeId roomTypeId);

	int sellableUnits(RoomTypeId roomTypeId);

	PriceQuote quote(RoomTypeId roomTypeId, StayPeriod period);

	record RoomTypeSnapshot(RoomTypeId id, UUID publicId, String name, short capacity) {
	}

	record PriceQuote(Money total, List<NightPrice> breakdown) {
		public PriceQuote {
			breakdown = List.copyOf(breakdown);
		}
	}

	record NightPrice(LocalDate date, Money price) {
	}
}
