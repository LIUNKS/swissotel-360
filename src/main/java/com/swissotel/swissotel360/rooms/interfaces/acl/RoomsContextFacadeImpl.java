package com.swissotel.swissotel360.rooms.interfaces.acl;

import com.swissotel.swissotel360.rooms.application.commandservices.RoomCommandService;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomOccupiedCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomCheckedOutCommand;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomView;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomNotFoundException;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoomsContextFacadeImpl implements RoomsContextFacade {
	private final RoomCommandService rooms;

	private final RoomQueryService queries;

	public RoomsContextFacadeImpl(RoomCommandService rooms, RoomQueryService queries) {
		this.rooms = rooms;
		this.queries = queries;
	}

	public Optional<RoomInfoSnapshot> findByPublicId(UUID id) {
		try {
			return Optional.of(info(queries.findByPublicId(new GetRoomByPublicIdQuery(id))));
		} catch (RoomNotFoundException ex) {
			return Optional.empty();
		}
	}

	public Optional<RoomInfoSnapshot> findById(Long id) {
		return queries.findById(id).map(RoomsContextFacadeImpl::info);
	}

	public Optional<RoomInfoSnapshot> findFirstAvailable(Long roomTypeId) {
		return queries.find(new RoomFilter(roomTypeId, RoomStatus.AVAILABLE, 0, 1)).content().stream().findFirst()
				.map(RoomsContextFacadeImpl::info);
	}

	private static RoomInfoSnapshot info(RoomView v) {
		var r = v.room();
		return new RoomInfoSnapshot(r.id(), r.publicId(), r.roomTypeId(), r.roomNumber(), r.status().name());
	}

	public RoomStatusSnapshot markOccupied(Long roomId) {
		var room = rooms.markOccupied(new MarkRoomOccupiedCommand(roomId));
		return new RoomStatusSnapshot(room.publicId(), room.status().name());
	}

	public RoomStatusSnapshot markCheckedOut(Long roomId) {
		var room = rooms.markCheckedOut(new MarkRoomCheckedOutCommand(roomId));
		return new RoomStatusSnapshot(room.publicId(), room.status().name());
	}
}
