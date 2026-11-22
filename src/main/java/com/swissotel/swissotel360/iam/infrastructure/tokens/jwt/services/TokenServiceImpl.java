package com.swissotel.swissotel360.iam.infrastructure.tokens.jwt.services;

import com.swissotel.swissotel360.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements BearerTokenService {
	private final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
	private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
	private static final String BEARER_TOKEN_PREFIX = "Bearer ";

	private final String secret;
	private final int expirationDays;

	public TokenServiceImpl(
			@Value("${authorization.jwt.secret}") String secret,
			@Value("${authorization.jwt.expiration.days}") int expirationDays) {
		this.secret = secret;
		this.expirationDays = expirationDays;
	}

	@Override
	public String generateToken(Long userId, UUID publicId, String email, String role, Collection<String> permissions) {
		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + expirationDays * 24L * 3600L * 1000L);
		return Jwts.builder()
				.subject(email)
				.claim("uid", userId)
				.claim("pid", publicId.toString())
				.claim("role", role)
				.claim("perms", List.copyOf(permissions))
				.issuedAt(issuedAt)
				.expiration(expiration)
				.signWith(getSigningKey())
				.compact();
	}

	@Override
	public Optional<AuthenticatedUserPrincipal> parsePrincipal(String token) {
		try {
			Claims claims = extractAllClaims(token);
			String email = claims.getSubject();
			Number uid = claims.get("uid", Number.class);
			String pidRaw = claims.get("pid", String.class);
			String role = claims.get("role", String.class);
			List<?> rawPerms = claims.get("perms", List.class);
			if (!StringUtils.hasText(email) || uid == null || !StringUtils.hasText(pidRaw) || !StringUtils.hasText(role)
					|| rawPerms == null) {
				logger.debug("JWT token missing required claims");
				return Optional.empty();
			}
			UUID publicId = UUID.fromString(pidRaw);
			Set<String> perms = rawPerms.stream()
					.filter(String.class::isInstance)
					.map(String.class::cast)
					.collect(Collectors.toUnmodifiableSet());
			return Optional.of(new AuthenticatedUserPrincipal(uid.longValue(), publicId, email, role, perms));
		} catch (Exception e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public String getUsernameFromToken(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	@Override
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			logger.debug("Token is valid");
			return true;
		} catch (Exception e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		}
		return false;
	}

	@Override
	public String getBearerTokenFrom(HttpServletRequest request) {
		String parameter = getAuthorizationParameterFrom(request);
		if (isTokenPresentIn(parameter) && isBearerTokenIn(parameter)) {
			return extractTokenFrom(parameter);
		}

		if (request.getCookies() != null) {
			for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
				if ("token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
		final Claims claims = extractAllClaims(token);
		return claimsResolvers.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	private String getAuthorizationParameterFrom(HttpServletRequest request) {
		return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
	}

	private boolean isTokenPresentIn(String authorizationParameter) {
		return StringUtils.hasText(authorizationParameter);
	}

	private boolean isBearerTokenIn(String authorizationParameter) {
		return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
	}

	private String extractTokenFrom(String authorizationHeader) {
		return authorizationHeader.substring(BEARER_TOKEN_PREFIX.length());
	}
}
