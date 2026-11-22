package com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stays")
@Getter
@Setter
public class StayPersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(name = "reservation_id", nullable = false, unique = true, updatable = false)
	private Long reservationId;
	@Column(name = "room_id", nullable = false, updatable = false)
	private Long roomId;
	@Column(name = "guest_id", nullable = false, updatable = false)
	private Long guestId;
	@Column(name = "checked_in_at", nullable = false, updatable = false)
	private Instant checkedInAt;
	@Column(name = "checked_out_at")
	private Instant checkedOutAt;
	@Column(nullable = false, length = 20)
	private String status;
	@Column(name = "checked_in_by", updatable = false)
	private Long checkedInBy;
}
