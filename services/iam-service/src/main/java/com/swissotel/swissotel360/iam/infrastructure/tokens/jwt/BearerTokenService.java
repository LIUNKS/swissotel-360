package com.swissotel.swissotel360.iam.infrastructure.tokens.jwt;

import com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Bearer Token Service interface.
 * Extends the application token port and adds HTTP request extraction
 * and stateless principal parsing capabilities.
 */
public interface BearerTokenService extends TokenService {
	String getBearerTokenFrom(HttpServletRequest request);

	Optional<AuthenticatedUserPrincipal> parsePrincipal(String token);
}
