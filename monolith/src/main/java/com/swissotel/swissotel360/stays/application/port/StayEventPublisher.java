package com.swissotel.swissotel360.stays.application.port;

import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import java.util.Collection;

public interface StayEventPublisher {
	/** @param saved persisted stay (carries the database id) used to build integration events */
	void publishAfterCommit(Stay saved, Collection<Object> domainEvents);
}
