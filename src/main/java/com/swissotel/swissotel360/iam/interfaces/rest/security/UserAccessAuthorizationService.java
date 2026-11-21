package com.swissotel.swissotel360.iam.interfaces.rest.security;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

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
	 * Checks whether the current authentication corresponds to the requested user id.
	 */
	public boolean isSelf(Authentication authentication, Long userId) {
		if (authentication == null || !authentication.isAuthenticated() || userId == null) {
			return false;
		}
		var email = authentication.getName();
		if (email == null || email.isBlank()) {
			return false;
		}
		var user = userQueryService.handle(new GetUserByEmailQuery(email));
		return user.isPresent() && userId.equals(user.get().getId());
	}
}
