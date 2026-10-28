package com.swissotel.swissotel360.rooms.domain.model.commands;
import java.util.UUID;
public record CreateRoomCommand(UUID roomTypePublicId,String roomNumber,Short floor) { }
