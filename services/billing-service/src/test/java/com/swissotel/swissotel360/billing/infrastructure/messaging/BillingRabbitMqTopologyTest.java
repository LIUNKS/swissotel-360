package com.swissotel.swissotel360.billing.infrastructure.messaging;

import com.swissotel.swissotel360.billing.interfaces.events.ServiceRequestCompletedEventMessage;
import com.swissotel.swissotel360.billing.interfaces.events.StayCheckedInEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingRabbitMqTopologyTest {

	private final BillingRabbitMqConfiguration config = new BillingRabbitMqConfiguration();

	@Test
	void declaresDurableExchangesDlqQueuesAndBindings() {
		var exchange = config.swissotelEventsExchange();
		var dlx = config.swissotelEventsDlx();
		var stayQueue = config.stayCheckedInQueue();
		var stayDlq = config.stayCheckedInDlq();
		var reqQueue = config.serviceRequestCompletedQueue();
		var reqDlq = config.serviceRequestCompletedDlq();

		assertEquals(BillingRabbitMqConfiguration.EVENTS_EXCHANGE, exchange.getName());
		assertTrue(exchange.isDurable());
		assertEquals(BillingRabbitMqConfiguration.EVENTS_DLX, dlx.getName());
		assertTrue(dlx.isDurable());

		assertEquals(BillingRabbitMqConfiguration.EVENTS_DLX, stayQueue.getArguments().get("x-dead-letter-exchange"));
		assertEquals(BillingRabbitMqConfiguration.STAY_CHECKED_IN_DLQ,
				stayQueue.getArguments().get("x-dead-letter-routing-key"));
		assertTrue(stayDlq.isDurable());

		assertEquals(BillingRabbitMqConfiguration.EVENTS_DLX, reqQueue.getArguments().get("x-dead-letter-exchange"));
		assertEquals(BillingRabbitMqConfiguration.SERVICE_REQUEST_COMPLETED_DLQ,
				reqQueue.getArguments().get("x-dead-letter-routing-key"));
		assertTrue(reqDlq.isDurable());

		assertEquals(BillingRabbitMqConfiguration.STAY_CHECKED_IN_ROUTING_KEY,
				config.stayCheckedInBinding(stayQueue, exchange).getRoutingKey());
		assertEquals(BillingRabbitMqConfiguration.STAY_CHECKED_IN_DLQ,
				config.stayCheckedInDlqBinding(stayDlq, dlx).getRoutingKey());
		assertEquals(BillingRabbitMqConfiguration.SERVICE_REQUEST_COMPLETED_ROUTING_KEY,
				config.serviceRequestCompletedBinding(reqQueue, exchange).getRoutingKey());
		assertEquals(BillingRabbitMqConfiguration.SERVICE_REQUEST_COMPLETED_DLQ,
				config.serviceRequestCompletedDlqBinding(reqDlq, dlx).getRoutingKey());
	}

	@Test
	void deserializesProducerPayloadsWithForeignTypeIdHeaderIntoConsumerRecords() {
		var converter = assertInstanceOf(JacksonJsonMessageConverter.class, config.rabbitJsonMessageConverter());

		var stayUuid = UUID.randomUUID();
		var stayJson = """
				{"stayPublicId":"%s","stayId":42,"reservationId":10,"guestId":20,"roomId":30,"checkedInAt":"2026-11-22T15:30:00Z"}"""
				.formatted(stayUuid);
		var stayProps = new MessageProperties();
		stayProps.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		stayProps.setHeader("__TypeId__",
				"com.swissotel.swissotel360.stays.interfaces.events.StayCheckedInIntegrationEvent");
		stayProps.setInferredArgumentType(StayCheckedInEventMessage.class);

		var convertedStay = assertInstanceOf(StayCheckedInEventMessage.class,
				converter.fromMessage(new Message(stayJson.getBytes(StandardCharsets.UTF_8), stayProps)));
		assertEquals(stayUuid, convertedStay.stayPublicId());
		assertEquals(42L, convertedStay.stayId());
		assertEquals(Instant.parse("2026-11-22T15:30:00Z"), convertedStay.checkedInAt());

		var reqUuid = UUID.randomUUID();
		var reqJson = """
				{"requestPublicId":"%s","serviceRequestId":77,"stayId":42,"serviceTypeCode":"spa","serviceTypeName":"Spa & Wellness","chargeAmount":150.50,"completedAt":"2026-11-22T16:45:00Z"}"""
				.formatted(reqUuid);
		var reqProps = new MessageProperties();
		reqProps.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		reqProps.setHeader("__TypeId__",
				"com.swissotel.swissotel360.servicerequests.interfaces.events.ServiceRequestCompletedIntegrationEvent");
		reqProps.setInferredArgumentType(ServiceRequestCompletedEventMessage.class);

		var convertedReq = assertInstanceOf(ServiceRequestCompletedEventMessage.class,
				converter.fromMessage(new Message(reqJson.getBytes(StandardCharsets.UTF_8), reqProps)));
		assertEquals(reqUuid, convertedReq.requestPublicId());
		assertEquals(77L, convertedReq.serviceRequestId());
		assertEquals(new BigDecimal("150.50"), convertedReq.chargeAmount());
	}
}
