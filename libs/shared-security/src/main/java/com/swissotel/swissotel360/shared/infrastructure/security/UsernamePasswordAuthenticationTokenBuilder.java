package com.swissotel.swissotel360.shared.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/**
 * Builds an authenticated Spring Security token from a resolved principal and request details.
 */
public final class UsernamePasswordAuthenticationTokenBuilder {

	private UsernamePasswordAuthenticationTokenBuilder() {
	}

	public static UsernamePasswordAuthenticationToken build(UserDetails principal, HttpServletRequest request) {
		var authenticationToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		return authenticationToken;
	}
}
