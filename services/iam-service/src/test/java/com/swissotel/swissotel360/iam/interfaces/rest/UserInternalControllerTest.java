package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByPublicIdQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserInternalControllerTest {

	private final UserQueryService users = mock(UserQueryService.class);
	private final UserInternalController controller = new UserInternalController(users);

	@Test
	void returnsInternalUserSnapshotByIdOrPublicIdAnd404WhenMissing() {
		var publicId = UUID.randomUUID();
		var user = new User(5L, publicId, "Jane Manager", "jane@swissotel.com", "hash",
				new Role(2L, Roles.ROLE_MANAGER), "active", null);

		when(users.handle(new GetUserByIdQuery(5L))).thenReturn(Optional.of(user));
		when(users.handle(new GetUserByPublicIdQuery(publicId))).thenReturn(Optional.of(user));
		when(users.handle(new GetUserByIdQuery(99L))).thenReturn(Optional.empty());
		when(users.handle(new GetUserByPublicIdQuery(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
				.thenReturn(Optional.empty());

		var byId = controller.getById(5L);
		assertEquals(HttpStatus.OK, byId.getStatusCode());
		assertNotNull(byId.getBody());
		assertEquals(5L, byId.getBody().id());
		assertEquals(publicId, byId.getBody().publicId());
		assertEquals("ROLE_MANAGER", byId.getBody().role());

		var byPublicId = controller.getByPublicId(publicId);
		assertEquals(HttpStatus.OK, byPublicId.getStatusCode());
		assertEquals(byId.getBody(), byPublicId.getBody());

		assertEquals(HttpStatus.NOT_FOUND, controller.getById(99L).getStatusCode());
		assertEquals(HttpStatus.NOT_FOUND,
				controller.getByPublicId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getStatusCode());
	}
}
