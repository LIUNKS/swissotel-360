package com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "digital_keys")
@Getter
@Setter
public class DigitalKeyPersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "stay_id", nullable = false, updatable = false)
	private Long stayId;
	@Column(name = "access_code", nullable = false, updatable = false, length = 64)
	private String accessCode;
	@Column(name = "activated_at", nullable = false, updatable = false)
	private Instant activatedAt;
	@Column(name = "revoked_at")
	private Instant revokedAt;
	@Column(nullable = false, length = 20)
	private String status;
}
