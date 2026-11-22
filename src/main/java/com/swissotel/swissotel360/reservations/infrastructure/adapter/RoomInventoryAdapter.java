package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import com.swissotel.swissotel360.rooms.interfaces.acl.AvailabilityContextFacade;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoomInventoryAdapter implements RoomInventoryPort {
	private final AvailabilityContextFacade rooms;

	public RoomInventoryAdapter(AvailabilityContextFacade rooms) {
		this.rooms = rooms;
	}

	@Override
	public Optional<RoomTypeSnapshot> findRoomType(UUID publicId) {
		return rooms.findRoomType(publicId).map(RoomInventoryAdapter::map);
	}

	@Override
	public Optional<RoomTypeSnapshot> findRoomTypeById(RoomTypeId id) {
		return rooms.findRoomTypeById(id.value()).map(RoomInventoryAdapter::map);
	}

	@Override
	public int sellableUnits(RoomTypeId id) {
		return rooms.sellableUnits(id.value());
	}

	@Override
	public PriceQuote quote(RoomTypeId id, StayPeriod period) {
		var snapshot = rooms.quote(id.value(), period.checkIn(), period.checkOut());
		return new PriceQuote(new Money(snapshot.totalAmount()),
				snapshot.breakdown().stream().map(n -> new NightPrice(n.date(), new Money(n.price()))).toList());
	}

	private static RoomTypeSnapshot map(com.swissotel.swissotel360.rooms.interfaces.acl.RoomTypeInventorySnapshot s) {
		return new RoomTypeSnapshot(new RoomTypeId(s.id()), s.publicId(), s.name(), s.capacity());
	}
}
