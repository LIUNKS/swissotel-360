package com.swissotel.swissotel360.billing.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingRabbitMqConfiguration {

	public static final String EVENTS_EXCHANGE = "swissotel.events";
	public static final String EVENTS_DLX = "swissotel.events.dlx";
	public static final String STAY_CHECKED_IN_ROUTING_KEY = "stay.checked-in";
	public static final String SERVICE_REQUEST_COMPLETED_ROUTING_KEY = "service-request.completed";
	public static final String STAY_CHECKED_IN_QUEUE = "billing.stay-checked-in.queue";
	public static final String STAY_CHECKED_IN_DLQ = "billing.stay-checked-in.dlq";
	public static final String SERVICE_REQUEST_COMPLETED_QUEUE = "billing.service-request-completed.queue";
	public static final String SERVICE_REQUEST_COMPLETED_DLQ = "billing.service-request-completed.dlq";

	@Bean
	public TopicExchange swissotelEventsExchange() {
		return new TopicExchange(EVENTS_EXCHANGE, true, false);
	}

	@Bean
	public DirectExchange swissotelEventsDlx() {
		return new DirectExchange(EVENTS_DLX, true, false);
	}

	@Bean
	public Queue stayCheckedInQueue() {
		return QueueBuilder.durable(STAY_CHECKED_IN_QUEUE)
				.deadLetterExchange(EVENTS_DLX)
				.deadLetterRoutingKey(STAY_CHECKED_IN_DLQ)
				.build();
	}

	@Bean
	public Queue stayCheckedInDlq() {
		return QueueBuilder.durable(STAY_CHECKED_IN_DLQ).build();
	}

	@Bean
	public Queue serviceRequestCompletedQueue() {
		return QueueBuilder.durable(SERVICE_REQUEST_COMPLETED_QUEUE)
				.deadLetterExchange(EVENTS_DLX)
				.deadLetterRoutingKey(SERVICE_REQUEST_COMPLETED_DLQ)
				.build();
	}

	@Bean
	public Queue serviceRequestCompletedDlq() {
		return QueueBuilder.durable(SERVICE_REQUEST_COMPLETED_DLQ).build();
	}

	@Bean
	public Binding stayCheckedInBinding(Queue stayCheckedInQueue, TopicExchange swissotelEventsExchange) {
		return BindingBuilder.bind(stayCheckedInQueue)
				.to(swissotelEventsExchange)
				.with(STAY_CHECKED_IN_ROUTING_KEY);
	}

	@Bean
	public Binding stayCheckedInDlqBinding(Queue stayCheckedInDlq, DirectExchange swissotelEventsDlx) {
		return BindingBuilder.bind(stayCheckedInDlq)
				.to(swissotelEventsDlx)
				.with(STAY_CHECKED_IN_DLQ);
	}

	@Bean
	public Binding serviceRequestCompletedBinding(Queue serviceRequestCompletedQueue,
			TopicExchange swissotelEventsExchange) {
		return BindingBuilder.bind(serviceRequestCompletedQueue)
				.to(swissotelEventsExchange)
				.with(SERVICE_REQUEST_COMPLETED_ROUTING_KEY);
	}

	@Bean
	public Binding serviceRequestCompletedDlqBinding(Queue serviceRequestCompletedDlq,
			DirectExchange swissotelEventsDlx) {
		return BindingBuilder.bind(serviceRequestCompletedDlq)
				.to(swissotelEventsDlx)
				.with(SERVICE_REQUEST_COMPLETED_DLQ);
	}

	@Bean
	public MessageConverter rabbitJsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
