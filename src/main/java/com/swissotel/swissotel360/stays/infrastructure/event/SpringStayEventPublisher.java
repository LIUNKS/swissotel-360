package com.swissotel.swissotel360.stays.infrastructure.event;

import com.swissotel.swissotel360.stays.application.port.StayEventPublisher;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.events.StayCheckedIn;
import com.swissotel.swissotel360.stays.domain.events.StayCheckedOut;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedInIntegrationEvent;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedOutIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class SpringStayEventPublisher implements StayEventPublisher {
	private final ApplicationEventPublisher publisher;

	public SpringStayEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publishAfterCommit(Stay saved, Collection<Object> domainEvents) {
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

	private static Object toIntegration(Stay saved, Object event) {
		return switch (event) {
			case StayCheckedIn e -> new StayCheckedInIntegrationEvent(e.id().value(), saved.dbId(), e.reservationId(),
					e.guestId(), e.roomId(), saved.checkedInAt());
			case StayCheckedOut e -> new StayCheckedOutIntegrationEvent(e.id().value(), saved.dbId(), e.guestId(),
					e.roomId(), saved.checkedOutAt());
			default -> null;
		};
	}
}
