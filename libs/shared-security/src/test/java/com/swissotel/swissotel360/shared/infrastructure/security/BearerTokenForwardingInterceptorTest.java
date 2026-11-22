package com.swissotel.swissotel360.shared.infrastructure.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class BearerTokenForwardingInterceptorTest {

	private final StatelessJwtTokenParser tokenParser = new StatelessJwtTokenParser(
			"swissotel-360-unit-test-jwt-secret-key-min-32-bytes");
	private final BearerTokenForwardingInterceptor interceptor = new BearerTokenForwardingInterceptor(tokenParser);

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void forwardsCookieTokenAsBearerAuthorizationHeaderWhenHeaderMissing() throws Exception {
		var incoming = new MockHttpServletRequest();
		incoming.setCookies(new Cookie("token", "cookie-jwt"));
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incoming));

		var outgoing = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://guests-service:8082/internal/guests/1"));
		ClientHttpRequestExecution execution = (req, body) -> mock(ClientHttpResponse.class);

		interceptor.intercept(outgoing, new byte[0], execution);

		assertEquals("Bearer cookie-jwt", outgoing.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
	}

	@Test
	void preservesExplicitAuthorizationHeaderAndHandlesMissingRequestContext() throws Exception {
		var incoming = new MockHttpServletRequest();
		incoming.addHeader(HttpHeaders.AUTHORIZATION, "Bearer caller-jwt");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incoming));

		var explicitOutgoing = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://iam-service:8081/internal/users/1"));
		explicitOutgoing.getHeaders().setBearerAuth("explicit-jwt");
		ClientHttpRequestExecution execution = (req, body) -> mock(ClientHttpResponse.class);

		interceptor.intercept(explicitOutgoing, new byte[0], execution);
		assertEquals("Bearer explicit-jwt", explicitOutgoing.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

		RequestContextHolder.resetRequestAttributes();
		var noContextOutgoing = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://rooms-service:8083/internal/rooms/1"));
		interceptor.intercept(noContextOutgoing, new byte[0], execution);
		assertNull(noContextOutgoing.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
	}
}
