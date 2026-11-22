package com.swissotel.swissotel360.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that populates Spring SecurityContext from a validated JWT
 * (Bearer header or session cookie) without calling any database or IAM service.
 * Registered directly inside SecurityFilterChain (not as a Spring bean) to prevent
 * duplicate servlet filter registration.
 */
public class StatelessBearerAuthorizationFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(StatelessBearerAuthorizationFilter.class);

	private final StatelessJwtTokenParser tokenParser;

	public StatelessBearerAuthorizationFilter(StatelessJwtTokenParser tokenParser) {
		this.tokenParser = tokenParser;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String token = tokenParser.extractBearerToken(request);
			if (token != null) {
				tokenParser.parsePrincipal(token).ifPresentOrElse(principal -> {
					var authentication = UsernamePasswordAuthenticationTokenBuilder.build(principal, request);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}, () -> log.debug("Provided token is not valid"));
			}
		} catch (Exception ex) {
			log.error("Cannot set user authentication: {}", ex.getMessage());
		}
		filterChain.doFilter(request, response);
	}
}
