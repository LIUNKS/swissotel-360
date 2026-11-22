package com.swissotel.swissotel360.billing.infrastructure.http;

import com.sun.net.httpserver.HttpServer;
import com.swissotel.swissotel360.shared.infrastructure.security.BearerTokenForwardingInterceptor;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessJwtTokenParser;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsHttpCollaboratorsAdapterTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void callsOperationsInternalEndpointsWithForwardedJwtAndMaps404ToEmpty() throws Exception {
		var stayPublicId = UUID.randomUUID();
		var reqPublicId = UUID.randomUUID();
		var lastAuthHeader = new AtomicReference<String>();

		var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			lastAuthHeader.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			int status = 404;
			String body = "";
			if (path.equals("/internal/stays/42") || path.equals("/internal/stays/by-public-id/" + stayPublicId)) {
				status = 200;
				body = """
						{"id":42,"publicId":"%s","reservationId":10,"guestId":20,"roomId":30,"status":"active","checkedInAt":"2026-11-22T15:00:00Z","checkedOutAt":null}"""
						.formatted(stayPublicId);
			} else if (path.equals("/internal/service-requests/77")
					|| path.equals("/internal/service-requests/by-public-id/" + reqPublicId)) {
				status = 200;
				body = """
						{"id":77,"publicId":"%s","stayId":42,"serviceTypeCode":"room_service","status":"completed"}"""
						.formatted(reqPublicId);
			} else if (path.equals("/internal/stays/500")) {
				status = 500;
				body = "{\"code\":\"INTERNAL_ERROR\"}";
			}
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
			exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
			if (bytes.length > 0) {
				try (var os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			} else {
				exchange.close();
			}
		});
		server.start();

		try {
			var incoming = new MockHttpServletRequest();
			incoming.setCookies(new Cookie("token", "forwarded-cookie-jwt"));
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incoming));

			var interceptor = new BearerTokenForwardingInterceptor(new StatelessJwtTokenParser(TEST_SECRET));
			var client = new OperationsClientConfiguration()
					.operationsInternalClient("http://localhost:" + server.getAddress().getPort(), interceptor);
			var adapter = new OperationsHttpCollaboratorsAdapter(client);

			var stayById = adapter.findStay(42L).orElseThrow();
			assertEquals(stayPublicId, stayById.publicId());
			assertEquals("Bearer forwarded-cookie-jwt", lastAuthHeader.get());

			var stayByUuid = adapter.findStay(stayPublicId).orElseThrow();
			assertEquals(42L, stayByUuid.id());

			var reqById = adapter.findServiceRequest(77L).orElseThrow();
			assertEquals(reqPublicId, reqById.publicId());

			var reqByUuid = adapter.findServiceRequest(reqPublicId).orElseThrow();
			assertEquals(77L, reqByUuid.id());

			assertTrue(adapter.findStay(999L).isEmpty());
			assertTrue(adapter.findServiceRequest(UUID.randomUUID()).isEmpty());
			assertThrows(HttpServerErrorException.class, () -> adapter.findStay(500L));
		} finally {
			server.stop(0);
		}
	}
}
