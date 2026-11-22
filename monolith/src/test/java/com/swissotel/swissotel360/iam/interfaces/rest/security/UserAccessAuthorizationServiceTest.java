package com.swissotel.swissotel360.iam.interfaces.rest.security;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserAccessAuthorizationServiceTest {

	private final UserQueryService userQueryService = mock(UserQueryService.class);
	private final UserAccessAuthorizationService service = new UserAccessAuthorizationService(userQueryService);

	@Test
	void isSelfUsesStatelessPrincipalWithoutQueryingIam() {
		var publicId = UUID.randomUUID();
		var principal = new AuthenticatedUserPrincipal(12L, publicId, "ana@swissotel.com", "ROLE_GUEST", Set.of());
		var auth = new UsernamePasswordAuthenticationToken(principal, "n/a", principal.getAuthorities());

		assertTrue(service.isSelf(auth, publicId));
		assertFalse(service.isSelf(auth, UUID.randomUUID()));
		verifyNoInteractions(userQueryService);
	}

	@Test
	void isSelfReturnsTrueWhenAuthenticatedUserMatchesRequestedPublicId() {
		var publicId = UUID.randomUUID();
		var user = new User(12L, publicId, "Ana Guest", "ana@swissotel.com", "encoded",
				new Role(5L, Roles.ROLE_GUEST), "active", null);
		when(userQueryService.handle(new GetUserByEmailQuery("ana@swissotel.com"))).thenReturn(Optional.of(user));
		var auth = new UsernamePasswordAuthenticationToken("ana@swissotel.com", "n/a", List.of());

		assertTrue(service.isSelf(auth, publicId));
	}

	@Test
	void isSelfReturnsFalseWhenAuthenticatedUserDoesNotMatchRequestedPublicId() {
		var user = new User(12L, UUID.randomUUID(), "Ana Guest", "ana@swissotel.com", "encoded",
				new Role(5L, Roles.ROLE_GUEST), "active", null);
		when(userQueryService.handle(new GetUserByEmailQuery("ana@swissotel.com"))).thenReturn(Optional.of(user));
		var auth = new UsernamePasswordAuthenticationToken("ana@swissotel.com", "n/a", List.of());

		assertFalse(service.isSelf(auth, UUID.randomUUID()));
	}

	@Test
	void isSelfReturnsFalseWhenUserDoesNotExistOrAuthenticationIsNull() {
		var targetPublicId = UUID.randomUUID();
		when(userQueryService.handle(new GetUserByEmailQuery("missing@swissotel.com"))).thenReturn(Optional.empty());
		var auth = new UsernamePasswordAuthenticationToken("missing@swissotel.com", "n/a", List.of());

		assertFalse(service.isSelf(auth, targetPublicId));
		assertFalse(service.isSelf(null, targetPublicId));

		var unauthenticated = new UsernamePasswordAuthenticationToken("ana@swissotel.com", "n/a");
		assertFalse(service.isSelf(unauthenticated, targetPublicId));
	}
}
