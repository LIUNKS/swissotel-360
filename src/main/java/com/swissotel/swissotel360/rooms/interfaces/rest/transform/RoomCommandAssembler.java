package com.swissotel.swissotel360.rooms.interfaces.rest.transform;

import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomStatusCommand;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.CreateRoomResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.UpdateRoomResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.UpdateRoomStatusResource;
import java.util.UUID;

public final class RoomCommandAssembler {
	private RoomCommandAssembler() {
	}

	public static CreateRoomCommand toCommand(CreateRoomResource resource) {
		return new CreateRoomCommand(resource.roomTypePublicId(), resource.roomNumber(), resource.floor());
	}

	public static UpdateRoomCommand toCommand(UUID publicId, UpdateRoomResource resource) {
		return new UpdateRoomCommand(publicId, resource.roomTypePublicId(), resource.floor());
	}

	public static UpdateRoomStatusCommand toStatusCommand(UUID publicId, UpdateRoomStatusResource resource) {
		return new UpdateRoomStatusCommand(publicId, resource.status());
	}
}
