package com.swissotel.swissotel360.reservations.infrastructure.event;

import com.swissotel.swissotel360.reservations.application.port.ReservationEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.Collection;
import java.util.List;

@Component
public class SpringReservationEventPublisher implements ReservationEventPublisher {
    private final ApplicationEventPublisher publisher;
    public SpringReservationEventPublisher(ApplicationEventPublisher publisher) { this.publisher=publisher; }
    @Override public void publishAfterCommit(Collection<Object> events) {
        var copy=List.copyOf(events);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { copy.forEach(publisher::publishEvent); }
            });
        } else copy.forEach(publisher::publishEvent);
    }
}
