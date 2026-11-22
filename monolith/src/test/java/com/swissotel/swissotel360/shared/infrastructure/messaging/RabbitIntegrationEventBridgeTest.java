package com.swissotel.swissotel360.shared.infrastructure.messaging;

import com.swissotel.swissotel360.servicerequests.interfaces.events.ServiceRequestCompletedIntegrationEvent;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedInIntegrationEvent;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedOutIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitIntegrationEventBridgeTest {

	private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
	private final RabbitIntegrationEventBridge bridge = new RabbitIntegrationEventBridge(rabbitTemplate);

	@Test
	void forwardsCommittedIntegrationEventsToExpectedRoutingKeys() {
		var stayCheckedIn = new StayCheckedInIntegrationEvent(
				UUID.randomUUID(), 12L, 34L, 56L, 78L, Instant.parse("2026-11-22T12:00:00Z"));
		var stayCheckedOut = new StayCheckedOutIntegrationEvent(
				UUID.randomUUID(), 12L, 56L, 78L, Instant.parse("2026-11-23T11:00:00Z"));
		var serviceCompleted = new ServiceRequestCompletedIntegrationEvent(
				UUID.randomUUID(), 90L, 12L, "room_service", "Room Service", new BigDecimal("45.00"),
				Instant.parse("2026-11-22T18:00:00Z"));

		bridge.onStayCheckedIn(stayCheckedIn);
		bridge.onStayCheckedOut(stayCheckedOut);
		bridge.onServiceRequestCompleted(serviceCompleted);

		verify(rabbitTemplate).convertAndSend(MonolithRabbitConfiguration.EVENTS_EXCHANGE,
				MonolithRabbitConfiguration.STAY_CHECKED_IN_ROUTING_KEY, stayCheckedIn);
		verify(rabbitTemplate).convertAndSend(MonolithRabbitConfiguration.EVENTS_EXCHANGE,
				MonolithRabbitConfiguration.STAY_CHECKED_OUT_ROUTING_KEY, stayCheckedOut);
		verify(rabbitTemplate).convertAndSend(MonolithRabbitConfiguration.EVENTS_EXCHANGE,
				MonolithRabbitConfiguration.SERVICE_REQUEST_COMPLETED_ROUTING_KEY, serviceCompleted);
	}

	@Test
	void swallowsBrokerOutagesWithoutFailingAlreadyCommittedOperation() {
		doThrow(new AmqpConnectException(new ConnectException("RabbitMQ down")))
				.when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

		var event = new StayCheckedInIntegrationEvent(
				UUID.randomUUID(), 12L, 34L, 56L, 78L, Instant.parse("2026-11-22T12:00:00Z"));

		assertDoesNotThrow(() -> bridge.onStayCheckedIn(event));
	}
}
