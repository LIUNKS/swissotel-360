package com.swissotel.swissotel360.billing.interfaces.events;

import com.swissotel.swissotel360.billing.application.command.RecordServiceRequestChargeCommand;
import com.swissotel.swissotel360.billing.application.command.RegisterFolioCommand;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.application.port.ProcessedEventStore;
import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import com.swissotel.swissotel360.billing.infrastructure.messaging.BillingRabbitMqConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ driving adapter consuming stay and service request integration events into billing commands.
 */
@Component
public class BillingIntegrationEventListener {

	private static final Logger log = LoggerFactory.getLogger(BillingIntegrationEventListener.class);

	private final BillingCommandService commands;
	private final BillingQueryService queries;
	private final ProcessedEventStore processedEvents;

	public BillingIntegrationEventListener(BillingCommandService commands, BillingQueryService queries,
			ProcessedEventStore processedEvents) {
		this.commands = commands;
		this.queries = queries;
		this.processedEvents = processedEvents;
	}

	@RabbitListener(queues = BillingRabbitMqConfiguration.STAY_CHECKED_IN_QUEUE)
	public void onStayCheckedIn(StayCheckedInEventMessage event) {
		if (event == null || event.stayId() == null) {
			throw new AmqpRejectAndDontRequeueException("Invalid StayCheckedIn event: missing stayId");
		}
		try {
			commands.registerFolio(new RegisterFolioCommand(event.stayId()));
		} catch (DataIntegrityViolationException ex) {
			if (queries.findFolioByStayId(event.stayId()).isPresent()
					|| processedEvents.exists("stay.checked-in:" + event.stayId())) {
				log.info("Duplicate StayCheckedIn event for stay {} handled idempotently", event.stayId());
				return;
			}
			throw ex;
		}
	}

	@RabbitListener(queues = BillingRabbitMqConfiguration.SERVICE_REQUEST_COMPLETED_QUEUE)
	public void onServiceRequestCompleted(ServiceRequestCompletedEventMessage event) {
		if (event == null || event.stayId() == null || event.serviceRequestId() == null) {
			throw new AmqpRejectAndDontRequeueException(
					"Invalid ServiceRequestCompleted event: missing stayId or serviceRequestId");
		}
		if (event.chargeAmount() == null || event.chargeAmount().signum() <= 0) {
			return;
		}
		var concept = (event.serviceTypeName() != null && !event.serviceTypeName().isBlank())
				? event.serviceTypeName()
				: event.serviceTypeCode();
		if (concept == null || concept.isBlank()) {
			throw new AmqpRejectAndDontRequeueException(
					"Invalid ServiceRequestCompleted event: missing service type concept");
		}
		try {
			commands.recordServiceRequestCharge(new RecordServiceRequestChargeCommand(
					event.stayId(), event.serviceRequestId(), concept, event.chargeAmount()));
		} catch (DataIntegrityViolationException ex) {
			if (processedEvents.exists("service-request.completed:" + event.serviceRequestId())) {
				log.info("Duplicate ServiceRequestCompleted event for service request {} handled idempotently",
						event.serviceRequestId());
				return;
			}
			throw ex;
		}
	}
}
