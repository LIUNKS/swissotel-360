package com.swissotel.swissotel360.rooms.domain.model.aggregates;

import java.util.UUID;

public record RoomType(Long id, UUID publicId, String name, Short capacity, String description) {
	public RoomType {
		if (publicId == null)
			throw new IllegalArgumentException("publicId is required");
		if (name == null || name.isBlank() || name.trim().length() > 60)
			throw new IllegalArgumentException("name must contain 1 to 60 characters");
		if (capacity == null || capacity <= 0)
			throw new IllegalArgumentException("capacity must be positive");
		name = name.trim();
	}

	public RoomType update(String name, Short capacity, String description) {
		return new RoomType(id, publicId, name, capacity, description);
	}
}
