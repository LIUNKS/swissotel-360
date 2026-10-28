package com.swissotel.swissotel360.iam.application.internal.eventhandlers;

import com.swissotel.swissotel360.iam.application.commandservices.RoleCommandService;
import com.swissotel.swissotel360.iam.domain.model.commands.SeedRolesCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Application lifecycle handler that ensures IAM roles are seeded when the
 * application is ready.
 */
@Service
@Slf4j
public class ApplicationReadyEventHandler {
	private final RoleCommandService roleCommandService;

	public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
		this.roleCommandService = roleCommandService;
	}

	/**
	 * Handles the Spring application-ready event and triggers role seeding
	 * verification.
	 *
	 * @param event
	 *                  Spring Boot readiness event
	 */
	@EventListener
	public void on(ApplicationReadyEvent event) {
		var applicationName = event.getApplicationContext().getId();
		log.info("Starting to verify if roles seeding is needed for {} at {}", applicationName, Instant.now());
		var seedRolesCommand = new SeedRolesCommand();
		roleCommandService.handle(seedRolesCommand);
		log.info("Roles seeding verification finished for {} at {}", applicationName, Instant.now());
	}
}
