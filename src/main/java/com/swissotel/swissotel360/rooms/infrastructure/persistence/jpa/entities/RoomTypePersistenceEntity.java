package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "room_types")
public class RoomTypePersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(nullable = false, unique = true, length = 60)
	private String name;
	@Column(nullable = false)
	private Short capacity;
	@Column(columnDefinition = "text")
	private String description;

	protected RoomTypePersistenceEntity() {
	}

	public RoomTypePersistenceEntity(Long id, UUID publicId, String name, Short capacity, String description) {
		this.id = id;
		this.publicId = publicId;
		this.name = name;
		this.capacity = capacity;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public UUID getPublicId() {
		return publicId;
	}

	public String getName() {
		return name;
	}

	public Short getCapacity() {
		return capacity;
	}

	public String getDescription() {
		return description;
	}
}
