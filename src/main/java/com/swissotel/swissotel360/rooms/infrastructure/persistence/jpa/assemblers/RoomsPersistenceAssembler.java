package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.assemblers;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.*;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.*;

public final class RoomsPersistenceAssembler {
	private RoomsPersistenceAssembler() {
	}

	public static RoomType toDomain(RoomTypePersistenceEntity e) {
		return new RoomType(e.getId(), e.getPublicId(), e.getName(), e.getCapacity(), e.getDescription());
	}

	public static RoomTypePersistenceEntity toEntity(RoomType d) {
		return new RoomTypePersistenceEntity(d.id(), d.publicId(), d.name(), d.capacity(), d.description());
	}

	public static Room toDomain(RoomPersistenceEntity e) {
		return new Room(e.getId(), e.getPublicId(), e.getRoomType().getId(), e.getRoomNumber(), e.getFloor(),
				e.getStatus());
	}

	public static RoomPersistenceEntity toEntity(Room d, RoomTypePersistenceEntity type) {
		return new RoomPersistenceEntity(d.id(), d.publicId(), type, d.roomNumber(), d.floor(), d.status());
	}

	public static Rate toDomain(RatePersistenceEntity e) {
		return new Rate(e.getId(), e.getRoomType().getId(), e.getRoomType().getPublicId(), e.getPrice(), e.getSeason(),
				e.getValidFrom(), e.getValidTo());
	}

	public static RatePersistenceEntity toEntity(Rate d, RoomTypePersistenceEntity type) {
		return new RatePersistenceEntity(d.id(), type, d.price(), d.season(), d.validFrom(), d.validTo());
	}
}
