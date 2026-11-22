package com.swissotel.swissotel360.rooms.application.internal.commandservices;

import com.swissotel.swissotel360.rooms.application.commandservices.RoomCommandService;
import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomNumberException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomStatusCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomOccupiedCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomCheckedOutCommand;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import java.util.UUID;

public class RoomCommandServiceImpl implements RoomCommandService {
	private final RoomRepository rooms;
	private final RoomTypeRepository roomTypes;

	public RoomCommandServiceImpl(RoomRepository rooms, RoomTypeRepository roomTypes) {
		this.rooms = rooms;
		this.roomTypes = roomTypes;
	}

	public Room create(CreateRoomCommand c) {
		var type = roomTypes.findByPublicId(c.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(c.roomTypePublicId()));
		var number = c.roomNumber().trim();
		if (rooms.existsByRoomNumber(number))
			throw new DuplicateRoomNumberException(number);
		return rooms.save(new Room(null, UUID.randomUUID(), type.id(), number, c.floor(), RoomStatus.AVAILABLE));
	}

	public Room update(UpdateRoomCommand c) {
		var room = rooms.findByPublicId(c.publicId()).orElseThrow(() -> new RoomNotFoundException(c.publicId()));
		var type = roomTypes.findByPublicId(c.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(c.roomTypePublicId()));
		return rooms.save(room.changeDetails(type.id(), c.floor()));
	}

	public Room updateStatus(UpdateRoomStatusCommand c) {
		var room = rooms.findByPublicId(c.roomPublicId())
				.orElseThrow(() -> new RoomNotFoundException(c.roomPublicId()));
		return rooms.save(room.transitionTo(c.status()));
	}

	public Room markOccupied(MarkRoomOccupiedCommand command) {
		var id = command.roomId();
		var room = rooms.findById(id).orElseThrow(() -> new RoomNotFoundException(id));
		return rooms.save(room.markOccupied());
	}

	public Room markCheckedOut(MarkRoomCheckedOutCommand command) {
		var id = command.roomId();
		var room = rooms.findById(id).orElseThrow(() -> new RoomNotFoundException(id));
		return rooms.save(room.markCheckedOut());
	}

	public void delete(DeleteRoomCommand command) {
		var id = command.publicId();
		var room = rooms.findByPublicId(id).orElseThrow(() -> new RoomNotFoundException(id));
		rooms.delete(room);
	}
}
