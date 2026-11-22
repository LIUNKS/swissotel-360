package com.swissotel.swissotel360.billing.application.internal.eventhandlers;

import com.swissotel.swissotel360.billing.application.command.RecordServiceRequestChargeCommand;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.servicerequests.interfaces.events.ServiceRequestCompletedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Service requests publishes after commit, so this is a plain listener running its own (new) transaction. */
@Component
public class ServiceRequestCompletedEventHandler {
	private static final Logger log = LoggerFactory.getLogger(ServiceRequestCompletedEventHandler.class);
	private final BillingCommandService commands;

	public ServiceRequestCompletedEventHandler(BillingCommandService commands) {
		this.commands = commands;
	}

	@EventListener
	public void on(ServiceRequestCompletedIntegrationEvent event) {
		if (event.chargeAmount() == null || event.chargeAmount().signum() <= 0)
			return;
		try {
			commands.recordServiceRequestCharge(new RecordServiceRequestChargeCommand(event.stayId(),
					event.serviceRequestId(), event.serviceTypeName(), event.chargeAmount()));
		} catch (RuntimeException e) {
			log.error("Could not charge service request {}", event.requestPublicId(), e);
		}
	}
}