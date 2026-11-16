package com.swissotel.swissotel360.billing.application.internal.eventhandlers;

import com.swissotel.swissotel360.billing.application.command.RegisterFolioCommand;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.stays.interfaces.events.StayCheckedInIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Stays publishes after commit, so this is a plain listener running its own (new) transaction. */
@Component
public class StayCheckedInEventHandler {
	private static final Logger log = LoggerFactory.getLogger(StayCheckedInEventHandler.class);
	private final BillingCommandService commands;

	public StayCheckedInEventHandler(BillingCommandService commands) {
		this.commands = commands;
	}

	@EventListener
	public void on(StayCheckedInIntegrationEvent event) {
		try {
			commands.registerFolio(new RegisterFolioCommand(event.stayId()));
		} catch (RuntimeException e) {
			// The stay is already committed; do not fail the check-in because of billing.
			log.error("Could not open folio for stay {}", event.stayId(), e);
		}
	}
}