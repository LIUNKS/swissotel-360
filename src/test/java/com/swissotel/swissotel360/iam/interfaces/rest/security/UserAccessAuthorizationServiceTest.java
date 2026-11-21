package com.swissotel.swissotel360.iam.interfaces.rest.security;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAccessAuthorizationServiceTest {

	private final UserQueryService userQueryService = mock(UserQueryService.class);
	private final UserAccessAuthorizationService service = new UserAccessAuthorizationService(userQueryService);

	@Test
	void isSelfReturnsTrueWhenAuthenticatedUserMatchesRequestedId() {
		var user = new User(12L, UUID.randomUUID(), "Ana Guest", "ana@swissotel.com", "encoded",
				new Role(5L, Roles.ROLE_GUEST), "active", null);
		when(userQueryService.handle(new GetUserByEmailQuery("ana@swissotel.com"))).thenReturn(Optional.of(user));
		var auth = new UsernamePasswordAuthenticationToken("ana@swissotel.com", "n/a", List.of());

		assertTrue(service.isSelf(auth, 12L));
	}

	@Test
	void isSelfReturnsFalseWhenAuthenticatedUserDoesNotMatchRequestedId() {
		var user = new User(12L, UUID.randomUUID(), "Ana Guest", "ana@swissotel.com", "encoded",
				new Role(5L, Roles.ROLE_GUEST), "active", null);
		when(userQueryService.handle(new GetUserByEmailQuery("ana@swissotel.com"))).thenReturn(Optional.of(user));
		var auth = new UsernamePasswordAuthenticationToken("ana@swissotel.com", "n/a", List.of());

		assertFalse(service.isSelf(auth, 1L));
	}

	@Test
	void isSelfReturnsFalseWhenUserDoesNotExistOrAuthenticationIsNull() {
		when(userQueryService.handle(new GetUserByEmailQuery("missing@swissotel.com"))).thenReturn(Optional.empty());
		var auth = new UsernamePasswordAuthenticationToken("missing@swissotel.com", "n/a", List.of());

		assertFalse(service.isSelf(auth, 12L));
		assertFalse(service.isSelf(null, 12L));

		var unauthenticated = new UsernamePasswordAuthenticationToken("ana@swissotel.com", "n/a");
		assertFalse(service.isSelf(unauthenticated, 12L));
	}
}
