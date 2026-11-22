package com.swissotel.swissotel360.gateway.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Blocks external callers from reaching service-to-service /internal/** endpoints through the API Gateway.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class InternalEndpointBlockingFilter extends OncePerRequestFilter {

	private static final String FORBIDDEN_BODY = """
			{"code":"FORBIDDEN","message":"Internal endpoint is not publicly accessible","details":"Direct access to /internal/** is blocked by API Gateway"}""";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String normalizedPath = normalizePath(request.getRequestURI(), request.getContextPath());
		if ("/internal".equals(normalizedPath) || normalizedPath.startsWith("/internal/")) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter().write(FORBIDDEN_BODY);
			response.getWriter().flush();
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static String normalizePath(String requestUri, String contextPath) {
		String path = requestUri == null ? "" : requestUri;
		if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		return path.replaceAll("/+", "/");
	}
}
