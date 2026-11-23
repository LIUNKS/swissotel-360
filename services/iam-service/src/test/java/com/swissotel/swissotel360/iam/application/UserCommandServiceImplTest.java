package com.swissotel.swissotel360.iam.application;

import com.swissotel.swissotel360.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.hashing.HashingService;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.commands.SignInCommand;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import com.swissotel.swissotel360.iam.domain.repositories.RolePermissionRepository;
import com.swissotel.swissotel360.iam.domain.repositories.RoleRepository;
import com.swissotel.swissotel360.iam.domain.repositories.UserRepository;
import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCommandServiceImplTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final RoleRepository roleRepository = mock(RoleRepository.class);
	private final RolePermissionRepository rolePermissionRepository = mock(RolePermissionRepository.class);
	private final HashingService hashingService = mock(HashingService.class);
	private final TokenService tokenService = mock(TokenService.class);

	private UserCommandServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new UserCommandServiceImpl(userRepository, roleRepository, rolePermissionRepository, hashingService,
				tokenService);
	}

	@Test
	void signInReturnsIdenticalUnauthorizedErrorForUnknownEmailAndWrongPassword() {
		var existingUser = new User(
				1L,
				UUID.randomUUID(),
				"John Doe",
				"john.doe@swissotel.com",
				"encoded-password",
				new Role(1L, Roles.ROLE_GUEST),
				"active",
				null);

		when(userRepository.findByEmail("missing@swissotel.com")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("john.doe@swissotel.com")).thenReturn(Optional.of(existingUser));
		when(hashingService.matches("wrong-password", "encoded-password")).thenReturn(false);

		var missingEmailResult = service.handle(new SignInCommand("missing@swissotel.com", "any-password"));
		var wrongPasswordResult = service.handle(new SignInCommand("john.doe@swissotel.com", "wrong-password"));

		var missingFailure = assertInstanceOf(Result.Failure.class, missingEmailResult);
		var wrongPasswordFailure = assertInstanceOf(Result.Failure.class, wrongPasswordResult);

		var missingError = assertInstanceOf(ApplicationError.class, missingFailure.error());
		var wrongPasswordError = assertInstanceOf(ApplicationError.class, wrongPasswordFailure.error());

		assertEquals("UNAUTHORIZED", missingError.code());
		assertEquals("Authentication failed", missingError.message());
		assertEquals("Invalid email or password", missingError.details());
		assertEquals(missingError, wrongPasswordError);

		verify(userRepository, never()).save(any());
		verify(tokenService, never()).generateToken(any(), any(), any(), any(), anyCollection());
	}

	@Test
	void signInReturnsUserAndTokenWhenCredentialsMatch() {
		var userPublicId = UUID.randomUUID();
		var existingUser = new User(
				1L,
				userPublicId,
				"John Doe",
				"john.doe@swissotel.com",
				"encoded-password",
				new Role(1L, Roles.ROLE_GUEST),
				"active",
				Instant.parse("2026-10-01T10:00:00Z"));
		var permissions = List.of("reservations.view", "reservations.create");

		when(userRepository.findByEmail("john.doe@swissotel.com")).thenReturn(Optional.of(existingUser));
		when(hashingService.matches("secretPassword123", "encoded-password")).thenReturn(true);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(rolePermissionRepository.findPermissionsByRoleId(1L)).thenReturn(permissions);
		when(tokenService.generateToken(1L, userPublicId, "john.doe@swissotel.com", "ROLE_GUEST", permissions))
				.thenReturn("jwt-token");

		var result = service.handle(new SignInCommand("john.doe@swissotel.com", "secretPassword123"));
		var success = assertInstanceOf(Result.Success.class, result);
		@SuppressWarnings("unchecked")
		var pair = (ImmutablePair<User, String>) success.value();

		assertEquals("john.doe@swissotel.com", pair.getLeft().getEmail());
		assertEquals("jwt-token", pair.getRight());
	}
}
