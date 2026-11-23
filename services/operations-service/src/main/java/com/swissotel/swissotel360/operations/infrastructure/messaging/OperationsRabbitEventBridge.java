package com.swissotel.swissotel360.operations.infrastructure.messaging;

import com.swissotel.swissotel360.servicerequests.interfaces.events.ServiceRequestCompletedIntegrationEvent;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedInIntegrationEvent;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedOutIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Publishes committed stay and service-request integration events to the RabbitMQ topic exchange.
 */
@Component
@ConditionalOnProperty(name = "swissotel.messaging.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class OperationsRabbitEventBridge {

	private static final Logger log = LoggerFactory.getLogger(OperationsRabbitEventBridge.class);

	private final RabbitTemplate rabbitTemplate;

	public OperationsRabbitEventBridge(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@EventListener
	public void onStayCheckedIn(StayCheckedInIntegrationEvent event) {
		publish(OperationsRabbitMqConfiguration.STAY_CHECKED_IN_ROUTING_KEY, event);
	}

	@EventListener
	public void onStayCheckedOut(StayCheckedOutIntegrationEvent event) {
		publish(OperationsRabbitMqConfiguration.STAY_CHECKED_OUT_ROUTING_KEY, event);
	}

	@EventListener
	public void onServiceRequestCompleted(ServiceRequestCompletedIntegrationEvent event) {
		publish(OperationsRabbitMqConfiguration.SERVICE_REQUEST_COMPLETED_ROUTING_KEY, event);
	}

	private void publish(String routingKey, Object event) {
		try {
			rabbitTemplate.convertAndSend(OperationsRabbitMqConfiguration.EVENTS_EXCHANGE, routingKey, event);
		} catch (AmqpException ex) {
			log.error("Failed to publish integration event {} to RabbitMQ exchange {}", routingKey,
					OperationsRabbitMqConfiguration.EVENTS_EXCHANGE, ex);
		}
	}
}
