package com.swissotel.swissotel360.rooms.interfaces.acl;

import com.swissotel.swissotel360.rooms.application.commandservices.RoomCommandService;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomOccupiedCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomCheckedOutCommand;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class RoomsContextFacadeImpl implements RoomsContextFacade {
	private final RoomCommandService rooms;

	public RoomsContextFacadeImpl(RoomCommandService rooms) {
		this.rooms = rooms;
	}

	public RoomStatusSnapshot markOccupied(UUID id) {
		var room = rooms.markOccupied(new MarkRoomOccupiedCommand(id));
		return new RoomStatusSnapshot(room.publicId(), room.status());
	}

	public RoomStatusSnapshot markCheckedOut(UUID id) {
		var room = rooms.markCheckedOut(new MarkRoomCheckedOutCommand(id));
		return new RoomStatusSnapshot(room.publicId(), room.status());
	}
}
