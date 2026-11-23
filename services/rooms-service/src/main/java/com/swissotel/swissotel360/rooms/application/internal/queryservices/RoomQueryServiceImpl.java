package com.swissotel.swissotel360.rooms.application.internal.queryservices;

import com.swissotel.swissotel360.rooms.application.queryservices.RoomQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomView;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomPageView;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import java.util.UUID;

public class RoomQueryServiceImpl implements RoomQueryService {
	private final RoomRepository rooms;
	private final RoomTypeRepository roomTypes;

	public RoomQueryServiceImpl(RoomRepository rooms, RoomTypeRepository roomTypes) {
		this.rooms = rooms;
		this.roomTypes = roomTypes;
	}

	public RoomPageView find(RoomFilter filter) {
		var page = rooms.find(filter);
		return new RoomPageView(page.content().stream().map(this::toView).toList(), page.totalElements());
	}

	@Override
	public RoomPageView find(UUID roomTypePublicId, RoomStatus status, int page, int size) {
		Long roomTypeId = null;
		if (roomTypePublicId != null) {
			roomTypeId = roomTypes.findByPublicId(roomTypePublicId)
					.orElseThrow(() -> new RoomTypeNotFoundException(roomTypePublicId))
					.id();
		}
		return find(new RoomFilter(roomTypeId, status, page, size));
	}

	public RoomView findByPublicId(GetRoomByPublicIdQuery query) {
		var id = query.publicId();
		return toView(rooms.findByPublicId(id).orElseThrow(() -> new RoomNotFoundException(id)));
	}

	public java.util.Optional<RoomView> findById(Long id) {
		return rooms.findById(id).map(this::toView);
	}

	private RoomView toView(Room room) {
		var type = roomTypes.findById(room.roomTypeId()).orElseThrow();
		return new RoomView(room, type.publicId(), type.name());
	}
}
