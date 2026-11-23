package com.swissotel.swissotel360.shared.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatelessJwtTokenParserTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";
	private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

	private final StatelessJwtTokenParser parser = new StatelessJwtTokenParser(TEST_SECRET);

	@Test
	void parsePrincipalExtractsAllClaimsFromValidToken() {
		var publicId = UUID.randomUUID();
		var token = Jwts.builder()
				.subject("admin@swissotel.com")
				.claim("uid", 42L)
				.claim("pid", publicId.toString())
				.claim("role", "ROLE_ADMIN")
				.claim("perms", List.of("users.manage", "billing.view"))
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(SIGNING_KEY)
				.compact();

		var parsed = parser.parsePrincipal(token);
		assertTrue(parsed.isPresent());
		var principal = parsed.get();
		assertEquals(42L, principal.userId());
		assertEquals(publicId, principal.publicId());
		assertEquals("admin@swissotel.com", principal.email());
		assertEquals("ROLE_ADMIN", principal.role());
		assertEquals(Set.of("users.manage", "billing.view"), principal.permissions());
		assertTrue(parser.validateToken(token));
		assertEquals("admin@swissotel.com", parser.getUsernameFromToken(token));
	}

	@Test
	void parsePrincipalRejectsInvalidSignatureExpiredOrIncompleteTokens() {
		var otherKey = Keys.hmacShaKeyFor("another-totally-different-secret-key-at-least-32-bytes"
				.getBytes(StandardCharsets.UTF_8));
		var publicId = UUID.randomUUID();

		var wrongSignature = Jwts.builder()
				.subject("admin@swissotel.com")
				.claim("uid", 42L)
				.claim("pid", publicId.toString())
				.claim("role", "ROLE_ADMIN")
				.claim("perms", List.of())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(otherKey)
				.compact();
		assertTrue(parser.parsePrincipal(wrongSignature).isEmpty());
		assertFalse(parser.validateToken(wrongSignature));

		var expired = Jwts.builder()
				.subject("admin@swissotel.com")
				.claim("uid", 42L)
				.claim("pid", publicId.toString())
				.claim("role", "ROLE_ADMIN")
				.claim("perms", List.of())
				.expiration(new Date(System.currentTimeMillis() - 10_000))
				.signWith(SIGNING_KEY)
				.compact();
		assertTrue(parser.parsePrincipal(expired).isEmpty());
		assertFalse(parser.validateToken(expired));

		var missingUid = Jwts.builder()
				.subject("admin@swissotel.com")
				.claim("pid", publicId.toString())
				.claim("role", "ROLE_ADMIN")
				.claim("perms", List.of())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(SIGNING_KEY)
				.compact();
		assertTrue(parser.parsePrincipal(missingUid).isEmpty());
	}

	@Test
	void extractBearerTokenPrefersHeaderThenFallsBackToCookie() {
		var bothRequest = new MockHttpServletRequest();
		bothRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
		bothRequest.setCookies(new Cookie("token", "cookie-token"));
		assertEquals("header-token", parser.extractBearerToken(bothRequest));

		var cookieOnlyRequest = new MockHttpServletRequest();
		cookieOnlyRequest.setCookies(new Cookie("other", "ignored"), new Cookie("token", "cookie-token"));
		assertEquals("cookie-token", parser.extractBearerToken(cookieOnlyRequest));

		var emptyRequest = new MockHttpServletRequest();
		assertNull(parser.extractBearerToken(emptyRequest));
	}
}
