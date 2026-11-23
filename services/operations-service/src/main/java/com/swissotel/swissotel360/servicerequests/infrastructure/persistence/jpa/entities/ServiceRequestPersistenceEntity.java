package com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
public class ServiceRequestPersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(name = "stay_id", nullable = false, updatable = false)
	private Long stayId;
	@Column(name = "service_type_id", nullable = false, updatable = false)
	private Long serviceTypeId;
	@Column(columnDefinition = "text", updatable = false)
	private String description;
	@Column(nullable = false, length = 20)
	private String status;
	@Column(name = "requested_at", nullable = false, updatable = false)
	private Instant requestedAt;
	@Column(name = "completed_at")
	private Instant completedAt;
	@Column(name = "handled_by")
	private Long handledBy;
}
