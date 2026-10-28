package com.swissotel.swissotel360.rooms.domain.model.commands;

import java.util.UUID;

public record MarkRoomCheckedOutCommand(UUID publicId) {
}
