package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "processed_integration_events")
@Getter
@Setter
public class ProcessedIntegrationEventPersistenceEntity {

	@Id
	@Column(name = "event_key", length = 120, nullable = false, updatable = false)
	private String eventKey;

	@Column(name = "event_type", length = 80, nullable = false, updatable = false)
	private String eventType;

	@Column(name = "processed_at", nullable = false, updatable = false)
	private Instant processedAt;
}
