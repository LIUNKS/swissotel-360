package com.swissotel.swissotel360.iam.application.commandservices;

import com.swissotel.swissotel360.iam.domain.model.commands.SeedRolesCommand;

/**
 * Application service contract for IAM role commands.
 */
public interface RoleCommandService {
	/**
	 * Handles the role seeding command.
	 *
	 * @param command
	 *                    role-seeding command
	 */
	void handle(SeedRolesCommand command);
}
