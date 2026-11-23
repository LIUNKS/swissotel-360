package com.swissotel.swissotel360.guests.infrastructure.http;

import com.sun.net.httpserver.HttpServer;
import com.swissotel.swissotel360.shared.infrastructure.security.BearerTokenForwardingInterceptor;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessJwtTokenParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IamHttpGuestAccountAdapterTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void resolvesIamInternalUserIdWithForwardedJwtAndMaps404ToEmpty() throws Exception {
		var userPublicId = UUID.randomUUID();
		var lastAuth = new AtomicReference<String>();

		var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			lastAuth.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/internal/users/by-public-id/" + userPublicId)) {
				byte[] bytes = """
						{"id":42,"publicId":"%s","email":"guest@swissotel.com","fullName":"Guest User","role":"ROLE_GUEST","status":"active"}"""
						.formatted(userPublicId).getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
				exchange.sendResponseHeaders(200, bytes.length);
				try (var os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			} else {
				exchange.sendResponseHeaders(404, -1);
				exchange.close();
			}
		});
		server.start();

		try {
			var request = new MockHttpServletRequest();
			request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer staff-jwt");
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

			var interceptor = new BearerTokenForwardingInterceptor(new StatelessJwtTokenParser(TEST_SECRET));
			var client = new IamClientConfiguration()
					.iamInternalClient("http://localhost:" + server.getAddress().getPort(), interceptor);
			var adapter = new IamHttpGuestAccountAdapter(client);

			assertEquals(Optional.of(42L), adapter.fetchInternalUserId(userPublicId));
			assertEquals("Bearer staff-jwt", lastAuth.get());
			assertTrue(adapter.fetchInternalUserId(UUID.randomUUID()).isEmpty());
		} finally {
			server.stop(0);
		}
	}
}
