package com.swissotel.swissotel360.rooms.domain.model.commands;

import java.util.UUID;

public record UpdateRoomCommand(UUID publicId, UUID roomTypePublicId, Short floor) {
}
