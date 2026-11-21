package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.UserResource;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsersControllerTest {

	private final UserQueryService userQueryService = mock(UserQueryService.class);
	private final UserCommandService userCommandService = mock(UserCommandService.class);

	private UsersController controller;
	private Locale previousDefaultLocale;

	@BeforeEach
	void setUp() {
		previousDefaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		controller = new UsersController(userQueryService, userCommandService);
	}

	@AfterEach
	void tearDown() {
		LocaleContextHolder.resetLocaleContext();
		Locale.setDefault(previousDefaultLocale);
	}

	@Test
	void getUserByIdReturns404WithErrorResourceWhenUserDoesNotExist() {
		when(userQueryService.handle(new GetUserByIdQuery(999L))).thenReturn(Optional.empty());

		var response = controller.getUserById(999L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		var body = assertInstanceOf(ErrorResource.class, response.getBody());
		assertEquals("USER_NOT_FOUND", body.code());
		assertEquals("User not found", body.message());
		assertNull(body.details());
	}

	@Test
	void getUserByIdReturns200WithUserResourceWhenUserExists() {
		var publicId = UUID.randomUUID();
		var user = new User(
				5L,
				publicId,
				"Jane Doe",
				"jane.doe@swissotel.com",
				"encoded",
				new Role(2L, Roles.ROLE_MANAGER),
				"active",
				null);
		when(userQueryService.handle(new GetUserByIdQuery(5L))).thenReturn(Optional.of(user));

		var response = controller.getUserById(5L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		var body = assertInstanceOf(UserResource.class, response.getBody());
		assertEquals(5L, body.id());
		assertEquals(publicId, body.publicId());
		assertEquals("jane.doe@swissotel.com", body.email());
		assertEquals("ROLE_MANAGER", body.role());
	}
}
