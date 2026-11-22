package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.hashing.HashingService;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import com.swissotel.swissotel360.iam.domain.repositories.RolePermissionRepository;
import com.swissotel.swissotel360.iam.domain.repositories.RoleRepository;
import com.swissotel.swissotel360.iam.domain.repositories.UserRepository;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.SignInResource;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationControllerTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final RoleRepository roleRepository = mock(RoleRepository.class);
	private final RolePermissionRepository rolePermissionRepository = mock(RolePermissionRepository.class);
	private final HashingService hashingService = mock(HashingService.class);
	private final TokenService tokenService = mock(TokenService.class);
	private final UserQueryService userQueryService = mock(UserQueryService.class);

	private UserCommandServiceImpl userCommandService;
	private AuthenticationController controller;
	private Locale previousDefaultLocale;

	@BeforeEach
	void setUp() {
		previousDefaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		userCommandService = new UserCommandServiceImpl(userRepository, roleRepository, rolePermissionRepository,
				hashingService, tokenService);
		controller = new AuthenticationController(userCommandService, userQueryService, false, "Lax", 7L);
	}

	@AfterEach
	void tearDown() {
		LocaleContextHolder.resetLocaleContext();
		Locale.setDefault(previousDefaultLocale);
	}

	@Test
	void signInReturnsIdentical401ErrorResourceForUnknownEmailAndWrongPassword() {
		var existingUser = new User(
				1L,
				UUID.randomUUID(),
				"John Doe",
				"john.doe@swissotel.com",
				"encoded-password",
				new Role(1L, Roles.ROLE_GUEST),
				"active",
				null);

		when(userRepository.findByEmail("unknown@swissotel.com")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("john.doe@swissotel.com")).thenReturn(Optional.of(existingUser));
		when(hashingService.matches("wrong-password", "encoded-password")).thenReturn(false);

		var unknownEmailResponse = controller.signIn(new SignInResource("unknown@swissotel.com", "any-password"));
		var wrongPasswordResponse = controller.signIn(new SignInResource("john.doe@swissotel.com", "wrong-password"));

		assertEquals(HttpStatus.UNAUTHORIZED, unknownEmailResponse.getStatusCode());
		assertEquals(HttpStatus.UNAUTHORIZED, wrongPasswordResponse.getStatusCode());

		var unknownEmailError = assertInstanceOf(ErrorResource.class, unknownEmailResponse.getBody());
		var wrongPasswordError = assertInstanceOf(ErrorResource.class, wrongPasswordResponse.getBody());

		assertEquals("UNAUTHORIZED", unknownEmailError.code());
		assertEquals("Invalid credentials", unknownEmailError.message());
		assertEquals("Invalid email or password", unknownEmailError.details());
		assertEquals(unknownEmailError, wrongPasswordError);
	}

	@Test
	void signInReturns200AndSessionCookieWhenCredentialsAreValid() {
		var userId = UUID.randomUUID();
		var existingUser = new User(
				1L,
				userId,
				"John Doe",
				"john.doe@swissotel.com",
				"encoded-password",
				new Role(1L, Roles.ROLE_GUEST),
				"active",
				null);

		when(userRepository.findByEmail("john.doe@swissotel.com")).thenReturn(Optional.of(existingUser));
		when(hashingService.matches("secretPassword123", "encoded-password")).thenReturn(true);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(rolePermissionRepository.findPermissionsByRoleId(1L)).thenReturn(List.of("reservations.create"));
		when(tokenService.generateToken(1L, userId, "john.doe@swissotel.com", "ROLE_GUEST",
				List.of("reservations.create"))).thenReturn("jwt-token-value");

		var response = controller.signIn(new SignInResource("john.doe@swissotel.com", "secretPassword123"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		var setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertNotNull(setCookie);
		assertTrue(setCookie.contains("token=jwt-token-value"));
		assertTrue(setCookie.contains("HttpOnly"));
		assertTrue(setCookie.contains("SameSite=Lax"));
		var body = assertInstanceOf(AuthenticatedUserResource.class, response.getBody());
		assertEquals(userId, body.publicId());
		assertEquals("john.doe@swissotel.com", body.email());
	}

	@Test
	void signInIncludesSecureFlagWhenConfiguredForHttps() {
		var secureController = new AuthenticationController(userCommandService, userQueryService, true, "Strict", 7L);
		var userPublicId = UUID.randomUUID();
		var existingUser = new User(
				1L,
				userPublicId,
				"John Doe",
				"john.doe@swissotel.com",
				"encoded-password",
				new Role(1L, Roles.ROLE_GUEST),
				"active",
				null);

		when(userRepository.findByEmail("john.doe@swissotel.com")).thenReturn(Optional.of(existingUser));
		when(hashingService.matches("secretPassword123", "encoded-password")).thenReturn(true);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(rolePermissionRepository.findPermissionsByRoleId(1L)).thenReturn(List.of("reservations.create"));
		when(tokenService.generateToken(1L, userPublicId, "john.doe@swissotel.com", "ROLE_GUEST",
				List.of("reservations.create"))).thenReturn("jwt-token-value");

		var response = secureController.signIn(new SignInResource("john.doe@swissotel.com", "secretPassword123"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		var setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertNotNull(setCookie);
		assertTrue(setCookie.contains("Secure"));
		assertTrue(setCookie.contains("SameSite=Strict"));
	}

	@Test
	void signOutClearsSessionCookie() {
		var response = controller.signOut();

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		var setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertNotNull(setCookie);
		assertTrue(setCookie.contains("token="));
		assertTrue(setCookie.contains("Max-Age=0"));
		assertTrue(setCookie.contains("HttpOnly"));
		assertTrue(setCookie.contains("SameSite=Lax"));
	}

	@Test
	void meReturnsAuthenticatedUserDetailsOr401WhenUserMissing() {
		var userId = UUID.randomUUID();
		var existingUser = new User(
				1L,
				userId,
				"John Doe",
				"john.doe@swissotel.com",
				"encoded-password",
				new Role(1L, Roles.ROLE_GUEST),
				"active",
				null);

		when(userQueryService.handle(new GetUserByIdQuery(1L)))
				.thenReturn(Optional.of(existingUser));
		var principal = new AuthenticatedUserPrincipal(1L, userId, "john.doe@swissotel.com", "ROLE_GUEST",
				Set.of("reservations.view"));
		var auth = new UsernamePasswordAuthenticationToken(principal, "n/a", principal.getAuthorities());

		var okResponse = controller.me(auth);
		assertEquals(HttpStatus.OK, okResponse.getStatusCode());
		var body = assertInstanceOf(AuthenticatedUserResource.class, okResponse.getBody());
		assertEquals(userId, body.publicId());
		assertEquals("john.doe@swissotel.com", body.email());

		when(userQueryService.handle(new GetUserByEmailQuery("missing@swissotel.com"))).thenReturn(Optional.empty());
		var missingAuth = new UsernamePasswordAuthenticationToken("missing@swissotel.com", "n/a", List.of());
		var missingResponse = controller.me(missingAuth);
		assertEquals(HttpStatus.UNAUTHORIZED, missingResponse.getStatusCode());
		var err = assertInstanceOf(ErrorResource.class, missingResponse.getBody());
		assertEquals("UNAUTHORIZED", err.code());
	}
}
