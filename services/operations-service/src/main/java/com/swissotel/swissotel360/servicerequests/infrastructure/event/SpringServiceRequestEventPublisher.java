package com.swissotel.swissotel360.servicerequests.infrastructure.event;

import com.swissotel.swissotel360.servicerequests.application.port.ServiceRequestEventPublisher;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.events.ServiceRequestCompleted;
import com.swissotel.swissotel360.servicerequests.interfaces.events.ServiceRequestCompletedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.Collection;
import java.util.Objects;

@Component
public class SpringServiceRequestEventPublisher implements ServiceRequestEventPublisher {
	private final ApplicationEventPublisher publisher;

	public SpringServiceRequestEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publishAfterCommit(ServiceRequest saved, Collection<Object> domainEvents) {
		var integration = domainEvents.stream().map(e -> toIntegration(saved, e)).filter(Objects::nonNull).toList();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					integration.forEach(publisher::publishEvent);
				}
			});
		} else
			integration.forEach(publisher::publishEvent);
	}

	private static Object toIntegration(ServiceRequest saved, Object event) {
		return switch (event) {
			case ServiceRequestCompleted e -> new ServiceRequestCompletedIntegrationEvent(e.id().value(),
					saved.dbId(), e.stayId(), e.serviceTypeCode(), e.serviceTypeName(), e.chargeAmount(),
					saved.completedAt());
			default -> null;
		};
	}
}
