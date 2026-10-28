package com.swissotel.swissotel360.iam.infrastructure.tokens.jwt;

import com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bearer Token Service interface.
 * Extends the application token port and adds HTTP request extraction
 * capabilities.
 */
public interface BearerTokenService extends TokenService {
	String getBearerTokenFrom(HttpServletRequest request);
}
