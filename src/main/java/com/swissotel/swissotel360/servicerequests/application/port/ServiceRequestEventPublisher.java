package com.swissotel.swissotel360.servicerequests.application.port;

import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import java.util.Collection;

public interface ServiceRequestEventPublisher {
	/** The saved request carries the database id used to build integration events. */
	void publishAfterCommit(ServiceRequest saved, Collection<Object> domainEvents);
}
