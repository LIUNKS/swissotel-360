package com.swissotel.swissotel360.shared.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StatelessBearerAuthorizationFilterTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	private final StatelessJwtTokenParser tokenParser = new StatelessJwtTokenParser(TEST_SECRET);
	private final StatelessBearerAuthorizationFilter filter = new StatelessBearerAuthorizationFilter(tokenParser);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void populatesSecurityContextFromValidBearerOrCookieToken() throws Exception {
		var publicId = UUID.randomUUID();
		var jwt = createValidJwt(publicId);
		var chain = mock(FilterChain.class);

		var request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(auth);
		var principal = assertInstanceOf(AuthenticatedUserPrincipal.class, auth.getPrincipal());
		assertEquals(9L, principal.userId());
		assertEquals(publicId, principal.publicId());
		assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("billing.charge")));

		SecurityContextHolder.clearContext();
		var cookieRequest = new MockHttpServletRequest();
		cookieRequest.setCookies(new Cookie("token", jwt));
		var cookieResponse = new MockHttpServletResponse();
		var cookieChain = mock(FilterChain.class);
		filter.doFilter(cookieRequest, cookieResponse, cookieChain);

		verify(cookieChain).doFilter(cookieRequest, cookieResponse);
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void leavesSecurityContextEmptyWhenTokenMissingOrInvalid() throws Exception {
		var chain = mock(FilterChain.class);
		var request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token");
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	private static String createValidJwt(UUID publicId) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject("staff@swissotel.com")
				.claim("uid", 9L)
				.claim("pid", publicId.toString())
				.claim("role", "ROLE_RECEPTIONIST")
				.claim("perms", List.of("billing.charge"))
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(key)
				.compact();
	}
}
