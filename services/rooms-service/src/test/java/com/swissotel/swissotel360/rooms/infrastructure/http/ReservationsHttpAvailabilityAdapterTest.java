package com.swissotel.swissotel360.rooms.infrastructure.http;

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
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationsHttpAvailabilityAdapterTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void queriesReservationsInternalEndpointsWithForwardedJwt() throws Exception {
		var lastAuth = new AtomicReference<String>();
		var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			lastAuth.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			String body = switch (path) {
				case "/internal/reservations/availability/overlapping" -> "{\"count\":3}";
				case "/internal/reservations/exists-by-room-type" -> "{\"hasReservations\":true}";
				default -> "";
			};
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
			exchange.sendResponseHeaders(bytes.length > 0 ? 200 : 404, bytes.length > 0 ? bytes.length : -1);
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
			var req = new MockHttpServletRequest();
			req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer rooms-caller-jwt");
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

			var interceptor = new BearerTokenForwardingInterceptor(new StatelessJwtTokenParser(TEST_SECRET));
			var client = new ReservationsClientConfiguration()
					.reservationsInternalClient("http://localhost:" + server.getAddress().getPort(), interceptor);
			var adapter = new ReservationsHttpAvailabilityAdapter(client);

			assertEquals(3L, adapter.countOverlappingActiveReservations(2L,
					LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 24)));
			assertEquals("Bearer rooms-caller-jwt", lastAuth.get());
			assertTrue(adapter.hasReservations(2L));
		} finally {
			server.stop(0);
		}
	}
}
