package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.ProcessedIntegrationEventPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedIntegrationEventPersistenceRepository
		extends JpaRepository<ProcessedIntegrationEventPersistenceEntity, String> {
}
