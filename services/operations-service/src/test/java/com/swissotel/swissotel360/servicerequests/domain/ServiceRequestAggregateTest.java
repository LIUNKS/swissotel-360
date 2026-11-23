package com.swissotel.swissotel360.servicerequests.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.events.ServiceRequestCompleted;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.InvalidServiceRequestTransitionException;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ServiceRequestAggregateTest {
	private static final Instant T0 = Instant.parse("2026-10-27T15:00:00Z");
	private static final Instant T1 = Instant.parse("2026-10-27T16:00:00Z");
	private static final ServiceType TYPE = new ServiceType(1L, "spa", "Spa", "Spa");

	private static ServiceRequest request() {
		return ServiceRequest.register(9L, TYPE, " towels ", T0);
	}

	@Test
	void walksRegisteredToInProgressToCompletedAndPublishesEvent() {
		var r = request();
		assertEquals(ServiceRequestStatus.REGISTERED, r.status());
		r.startProgress(5L);
		r.complete(5L, new BigDecimal("12.50"), T1);
		assertEquals(ServiceRequestStatus.COMPLETED, r.status());
		assertEquals(T1, r.completedAt());
		assertTrue(r.domainEvents().stream().anyMatch(ServiceRequestCompleted.class::isInstance));
	}

	@Test
	void canCompleteDirectlyButRejectsIllegalTransitions() {
		var r = request();
		r.complete(5L, null, T1);
		assertThrows(InvalidServiceRequestTransitionException.class, () -> r.complete(5L, null, T1));
		assertThrows(InvalidServiceRequestTransitionException.class, () -> r.startProgress(5L));
		assertThrows(IllegalArgumentException.class, () -> request().complete(5L, new BigDecimal("-1"), T1));
	}
}
