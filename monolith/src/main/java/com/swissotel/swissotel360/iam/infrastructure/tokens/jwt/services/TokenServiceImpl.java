package com.swissotel.swissotel360.iam.infrastructure.tokens.jwt.services;

import com.swissotel.swissotel360.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessJwtTokenParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenServiceImpl implements BearerTokenService {

	private final StatelessJwtTokenParser tokenParser;
	private final String secret;
	private final int expirationDays;

	public TokenServiceImpl(
			StatelessJwtTokenParser tokenParser,
			@Value("${authorization.jwt.secret}") String secret,
			@Value("${authorization.jwt.expiration.days}") int expirationDays) {
		this.tokenParser = tokenParser;
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
		return tokenParser.parsePrincipal(token);
	}

	@Override
	public String getUsernameFromToken(String token) {
		return tokenParser.getUsernameFromToken(token);
	}

	@Override
	public boolean validateToken(String token) {
		return tokenParser.validateToken(token);
	}

	@Override
	public String getBearerTokenFrom(HttpServletRequest request) {
		return tokenParser.extractBearerToken(request);
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
}
