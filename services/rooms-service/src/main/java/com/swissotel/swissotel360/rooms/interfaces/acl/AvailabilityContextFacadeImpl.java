package com.swissotel.swissotel360.rooms.interfaces.acl;

import com.swissotel.swissotel360.rooms.application.queryservices.AvailabilityQueryService;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.model.queries.QuoteRoomTypeQuery;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class AvailabilityContextFacadeImpl implements AvailabilityContextFacade {
	private final AvailabilityQueryService availability;
	private final RoomRepository rooms;
	private final RoomTypeRepository types;

	public AvailabilityContextFacadeImpl(AvailabilityQueryService availability, RoomRepository rooms,
			RoomTypeRepository types) {
		this.availability = availability;
		this.rooms = rooms;
		this.types = types;
	}

	public int countAvailable(UUID id, LocalDate checkIn, LocalDate checkOut) {
		return availability.countAvailable(new QuoteRoomTypeQuery(id, checkIn, checkOut));
	}

	public RoomAvailabilitySnapshot quote(UUID id, LocalDate checkIn, LocalDate checkOut) {
		var quote = availability.quote(new QuoteRoomTypeQuery(id, checkIn, checkOut));
		return new RoomAvailabilitySnapshot(quote.roomTypePublicId(), quote.checkIn(), quote.checkOut(), quote.nights(),
				quote.totalAmount(), quote.breakdown().stream()
						.map(n -> new RoomAvailabilitySnapshot.NightPrice(n.date(), n.price())).toList());
	}

	public java.util.Optional<RoomTypeInventorySnapshot> findRoomType(UUID id) {
		return types.findByPublicId(id)
				.map(t -> new RoomTypeInventorySnapshot(t.id(), t.publicId(), t.name(), t.capacity()));
	}

	public java.util.Optional<RoomTypeInventorySnapshot> findRoomTypeById(Long id) {
		return types.findById(id).map(t -> new RoomTypeInventorySnapshot(t.id(), t.publicId(), t.name(), t.capacity()));
	}

	public int sellableUnits(Long id) {
		return Math.toIntExact(rooms.countByRoomTypeIdAndStatusNot(id,
				com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus.OUT_OF_SERVICE));
	}

	public RoomAvailabilitySnapshot quote(Long id, LocalDate checkIn, LocalDate checkOut) {
		var type = types.findById(id).orElseThrow(() -> new RoomTypeNotFoundException(id));
		return quote(type.publicId(), checkIn, checkOut);
	}
}
