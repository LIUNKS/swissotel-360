package com.swissotel.swissotel360.iam.interfaces.rest.transform;

import com.swissotel.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {
	private SignUpCommandFromResourceAssembler() {
	}

	public static SignUpCommand toCommandFromResource(SignUpResource resource) {
		return new SignUpCommand(resource.fullName(), resource.email(), resource.password(), Roles.ROLE_GUEST);
	}
}
