package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.billing.application.port.ProcessedEventStore;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.ProcessedIntegrationEventPersistenceEntity;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories.ProcessedIntegrationEventPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class ProcessedEventStoreImpl implements ProcessedEventStore {

	private final ProcessedIntegrationEventPersistenceRepository repository;

	public ProcessedEventStoreImpl(ProcessedIntegrationEventPersistenceRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean exists(String eventKey) {
		return repository.existsById(eventKey);
	}

	@Override
	public void markProcessed(String eventKey, String eventType, Instant processedAt) {
		if (repository.existsById(eventKey)) {
			return;
		}
		var entity = new ProcessedIntegrationEventPersistenceEntity();
		entity.setEventKey(eventKey);
		entity.setEventType(eventType);
		entity.setProcessedAt(processedAt);
		repository.saveAndFlush(entity);
	}
}
