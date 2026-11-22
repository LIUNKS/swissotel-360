package com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.pipeline;

import com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.swissotel.swissotel360.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
	private final BearerTokenService tokenService;

	public BearerAuthorizationRequestFilter(BearerTokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String token = tokenService.getBearerTokenFrom(request);
			if (token != null) {
				tokenService.parsePrincipal(token).ifPresentOrElse(principal -> {
					var authentication = UsernamePasswordAuthenticationTokenBuilder.build(principal, request);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}, () -> LOGGER.debug("Provided token is not valid"));
			}
		} catch (Exception e) {
			LOGGER.error("Cannot set user authentication: {}", e.getMessage());
		}
		filterChain.doFilter(request, response);
	}
}
