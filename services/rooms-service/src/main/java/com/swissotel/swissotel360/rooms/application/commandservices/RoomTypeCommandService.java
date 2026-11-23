package com.swissotel.swissotel360.rooms.application.commandservices;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRoomTypeCommand;
import java.util.UUID;

public interface RoomTypeCommandService {
	RoomType create(CreateRoomTypeCommand command);

	RoomType update(UpdateRoomTypeCommand command);

	void delete(DeleteRoomTypeCommand command);
}
