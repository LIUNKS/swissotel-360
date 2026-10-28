package com.swissotel.swissotel360.rooms.domain.model.commands;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import java.util.UUID;
public record UpdateRoomStatusCommand(UUID roomPublicId,RoomStatus status) { }
