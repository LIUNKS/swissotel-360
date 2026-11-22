package com.swissotel.swissotel360.iam.interfaces.rest.security;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Authorization helper for verifying whether the authenticated user owns a user resource.
 */
@Component("userAccessAuthorizationService")
public class UserAccessAuthorizationService {
	private final UserQueryService userQueryService;

	public UserAccessAuthorizationService(UserQueryService userQueryService) {
		this.userQueryService = userQueryService;
	}

	/**
	 * Checks whether the current authentication corresponds to the requested user public id.
	 */
	public boolean isSelf(Authentication authentication, UUID userPublicId) {
		if (authentication == null || !authentication.isAuthenticated() || userPublicId == null) {
			return false;
		}
		if (authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
			return userPublicId.equals(principal.publicId());
		}
		var email = authentication.getName();
		if (email == null || email.isBlank()) {
			return false;
		}
		var user = userQueryService.handle(new GetUserByEmailQuery(email));
		return user.isPresent() && userPublicId.equals(user.get().getPublicId());
	}
}
