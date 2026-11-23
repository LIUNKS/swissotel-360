package com.swissotel.swissotel360.operations.infrastructure.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "swissotel.messaging.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class OperationsRabbitMqConfiguration {

	public static final String EVENTS_EXCHANGE = "swissotel.events";
	public static final String STAY_CHECKED_IN_ROUTING_KEY = "stay.checked-in";
	public static final String STAY_CHECKED_OUT_ROUTING_KEY = "stay.checked-out";
	public static final String SERVICE_REQUEST_COMPLETED_ROUTING_KEY = "service-request.completed";

	@Bean
	public TopicExchange swissotelEventsExchange() {
		return new TopicExchange(EVENTS_EXCHANGE, true, false);
	}

	@Bean
	public MessageConverter rabbitJsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
