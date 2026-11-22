package com.swissotel.swissotel360.shared.infrastructure.security;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Propagates the incoming request JWT (from Authorization header or session cookie)
 * to outbound RestClient HTTP calls so downstream internal endpoints receive caller context.
 */
public class BearerTokenForwardingInterceptor implements ClientHttpRequestInterceptor {

	private final StatelessJwtTokenParser tokenParser;

	public BearerTokenForwardingInterceptor(StatelessJwtTokenParser tokenParser) {
		this.tokenParser = tokenParser;
	}

	@Override
	public @NonNull ClientHttpResponse intercept(
			@NonNull HttpRequest request,
			byte @NonNull [] body,
			@NonNull ClientHttpRequestExecution execution) throws IOException {
		if (request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
			var attributes = RequestContextHolder.getRequestAttributes();
			if (attributes instanceof ServletRequestAttributes servletAttributes) {
				String token = tokenParser.extractBearerToken(servletAttributes.getRequest());
				if (StringUtils.hasText(token)) {
					request.getHeaders().setBearerAuth(token);
				}
			}
		}
		return execution.execute(request, body);
	}
}
