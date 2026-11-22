package com.swissotel.swissotel360.iam.interfaces.rest.transform;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
	private AuthenticatedUserResourceFromEntityAssembler() {
	}

	public static AuthenticatedUserResource toResourceFromEntity(User entity) {
		return new AuthenticatedUserResource(entity.getPublicId(), entity.getFullName(),
				entity.getEmail(), entity.getRole().getStringName());
	}
}
