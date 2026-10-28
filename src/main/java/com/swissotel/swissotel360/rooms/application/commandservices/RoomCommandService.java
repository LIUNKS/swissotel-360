package com.swissotel.swissotel360.rooms.application.commandservices;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomStatusCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomOccupiedCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.MarkRoomCheckedOutCommand;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import java.util.UUID;
public interface RoomCommandService { Room create(CreateRoomCommand command); Room update(UpdateRoomCommand command); Room updateStatus(UpdateRoomStatusCommand command); Room markOccupied(MarkRoomOccupiedCommand command); Room markCheckedOut(MarkRoomCheckedOutCommand command); void delete(DeleteRoomCommand command); }
