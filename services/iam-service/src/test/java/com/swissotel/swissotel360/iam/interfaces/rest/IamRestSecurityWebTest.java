package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.commands.SignInCommand;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByPublicIdQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.configuration.WebSecurityConfiguration;
import com.swissotel.swissotel360.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.swissotel.swissotel360.iam.interfaces.rest.security.UserAccessAuthorizationService;
import com.swissotel.swissotel360.shared.application.result.Result;
import com.swissotel.swissotel360.shared.infrastructure.autoconfigure.SharedKernelAutoConfiguration;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessSecurityAutoConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { AuthenticationController.class, UsersController.class })
@ImportAutoConfiguration({
		SecurityAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		StatelessSecurityAutoConfiguration.class,
		SharedKernelAutoConfiguration.class
})
@Import({ WebSecurityConfiguration.class, UserAccessAuthorizationService.class })
@TestPropertySource(properties = {
		"authorization.jwt.secret=swissotel-360-unit-test-jwt-secret-key-min-32-bytes",
		"authorization.jwt.expiration.days=7"
})
class IamRestSecurityWebTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserCommandService userCommandService;

	@MockitoBean
	private UserQueryService userQueryService;

	@MockitoBean(name = "defaultUserDetailsService")
	private UserDetailsService defaultUserDetailsService;

	@MockitoBean
	private BCryptHashingService hashingService;

	@Test
	void enforcesJwtAndRbacAndExposesOnlyPublicIds() throws Exception {
		var guestPublicId = UUID.randomUUID();
		var otherPublicId = UUID.randomUUID();
		var guestUser = new User(12L, guestPublicId, "Guest User", "guest@swissotel.com", "hash",
				new Role(5L, Roles.ROLE_GUEST), "active", null);

		when(userCommandService.handle(any(SignInCommand.class)))
				.thenReturn(Result.success(ImmutablePair.of(guestUser, "signed-jwt")));
		when(userQueryService.handle(new GetUserByIdQuery(12L))).thenReturn(Optional.of(guestUser));
		when(userQueryService.handle(new GetUserByPublicIdQuery(guestPublicId))).thenReturn(Optional.of(guestUser));

		mockMvc.perform(get("/api/v1/authentication/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(post("/api/v1/authentication/sign-in")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"guest@swissotel.com","password":"Password123!"}"""))
				.andExpect(status().isOk())
				.andExpect(header().exists(HttpHeaders.SET_COOKIE))
				.andExpect(jsonPath("$.publicId").value(guestPublicId.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		String guestToken = createJwt(12L, guestPublicId, "guest@swissotel.com", "ROLE_GUEST",
				List.of("reservations.view"));

		mockMvc.perform(get("/api/v1/authentication/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.publicId").value(guestPublicId.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(get("/api/v1/users/{id}", guestPublicId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.publicId").value(guestPublicId.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(get("/api/v1/users/{id}", otherPublicId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private static String createJwt(Long userId, UUID publicId, String email, String role, List<String> permissions) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject(email)
				.claim("uid", userId)
				.claim("pid", publicId.toString())
				.claim("role", role)
				.claim("perms", permissions)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(key)
				.compact();
	}
}
