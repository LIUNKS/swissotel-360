package com.swissotel.swissotel360.rooms.domain.model.commands;

import java.util.UUID;

public record UpdateRoomTypeCommand(UUID publicId, String name, Short capacity, String description) {
}
