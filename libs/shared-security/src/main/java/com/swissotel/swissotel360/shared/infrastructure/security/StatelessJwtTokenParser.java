package com.swissotel.swissotel360.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stateless JWT verifier and HTTP bearer/cookie extractor shared across services.
 * Verifies signatures and claims locally without calling IAM persistence.
 */
public class StatelessJwtTokenParser {
	private static final Logger log = LoggerFactory.getLogger(StatelessJwtTokenParser.class);
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String SESSION_COOKIE_NAME = "token";

	private final SecretKey signingKey;

	public StatelessJwtTokenParser(String secret) {
		if (!StringUtils.hasText(secret)) {
			throw new IllegalArgumentException("JWT secret must not be blank");
		}
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public Optional<AuthenticatedUserPrincipal> parsePrincipal(@Nullable String token) {
		if (!StringUtils.hasText(token)) {
			return Optional.empty();
		}
		try {
			Claims claims = extractAllClaims(token);
			String email = claims.getSubject();
			Number uid = claims.get("uid", Number.class);
			String pidRaw = claims.get("pid", String.class);
			String role = claims.get("role", String.class);
			List<?> rawPerms = claims.get("perms", List.class);
			if (!StringUtils.hasText(email) || uid == null || !StringUtils.hasText(pidRaw)
					|| !StringUtils.hasText(role) || rawPerms == null) {
				log.debug("JWT token missing required claims");
				return Optional.empty();
			}
			UUID publicId = UUID.fromString(pidRaw);
			Set<String> perms = rawPerms.stream()
					.filter(String.class::isInstance)
					.map(String.class::cast)
					.collect(Collectors.toUnmodifiableSet());
			return Optional.of(new AuthenticatedUserPrincipal(uid.longValue(), publicId, email, role, perms));
		} catch (Exception ex) {
			log.debug("Invalid JWT token: {}", ex.getMessage());
			return Optional.empty();
		}
	}

	public boolean validateToken(@Nullable String token) {
		if (!StringUtils.hasText(token)) {
			return false;
		}
		try {
			Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
			return true;
		} catch (Exception ex) {
			log.debug("Invalid JWT token: {}", ex.getMessage());
			return false;
		}
	}

	public String getUsernameFromToken(String token) {
		return extractAllClaims(token).getSubject();
	}

	public @Nullable String extractBearerToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
			String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
			if (StringUtils.hasText(token)) {
				return token;
			}
		}

		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (SESSION_COOKIE_NAME.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
	}
}
