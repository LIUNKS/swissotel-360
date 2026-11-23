package com.swissotel.swissotel360.operations.infrastructure.http;

import com.sun.net.httpserver.HttpServer;
import com.swissotel.swissotel360.shared.infrastructure.security.BearerTokenForwardingInterceptor;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessJwtTokenParser;
import com.swissotel.swissotel360.stays.interfaces.acl.StaysContextFacade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class OperationsHttpAdaptersTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
		SecurityContextHolder.clearContext();
	}

	@Test
	void invokesGuestsRoomsAndReservationsInternalEndpointsWithForwardedJwt() throws Exception {
		var guestUuid = UUID.randomUUID();
		var roomUuid = UUID.randomUUID();
		var resUuid = UUID.randomUUID();
		var roomCalls = new CopyOnWriteArrayList<String>();
		var resCalls = new CopyOnWriteArrayList<String>();
		var authHeaders = new CopyOnWriteArrayList<String>();

		var guestsServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		guestsServer.createContext("/", exchange -> {
			authHeaders.add(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/internal/guests/20") || path.equals("/internal/guests/by-user-id/9")) {
				byte[] bytes = """
						{"id":20,"publicId":"%s","userId":9,"fullName":"Ada Lovelace","email":"ada@swissotel.com","documentTypeCode":"DNI","documentNumber":"12345678"}"""
						.formatted(guestUuid).getBytes(StandardCharsets.UTF_8);
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
		guestsServer.start();

		var roomsServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		roomsServer.createContext("/", exchange -> {
			authHeaders.add(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			roomCalls.add(exchange.getRequestMethod() + " " + path);
			int status = 404;
			String body = "";
			if (path.equals("/internal/rooms/30") || path.equals("/internal/rooms/by-public-id/" + roomUuid)
					|| path.equals("/internal/rooms/available")) {
				status = 200;
				body = """
						{"id":30,"publicId":"%s","roomTypeId":2,"roomNumber":"301","status":"AVAILABLE"}"""
						.formatted(roomUuid);
			} else if (path.equals("/internal/rooms/30/occupy")) {
				status = 200;
				body = """
						{"publicId":"%s","status":"OCCUPIED"}""".formatted(roomUuid);
			} else if (path.equals("/internal/rooms/30/check-out")) {
				status = 200;
				body = """
						{"publicId":"%s","status":"CLEANING"}""".formatted(roomUuid);
			} else if (path.equals("/internal/rooms/30/release")) {
				status = 200;
				body = """
						{"publicId":"%s","status":"AVAILABLE"}""".formatted(roomUuid);
			}
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
			exchange.sendResponseHeaders(status, bytes.length > 0 ? bytes.length : -1);
			if (bytes.length > 0) {
				try (var os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			} else {
				exchange.close();
			}
		});
		roomsServer.start();

		var reservationsServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		reservationsServer.createContext("/", exchange -> {
			authHeaders.add(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			resCalls.add(exchange.getRequestMethod() + " " + path);
			if (path.equals("/internal/reservations/by-code/SW26-ABC")) {
				byte[] bytes = """
						{"publicId":"%s","code":"SW26-ABC","guestId":20,"roomTypeId":2,"checkIn":"2026-11-22","checkOut":"2026-11-24","status":"confirmed","internalId":100}"""
						.formatted(resUuid).getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
				exchange.sendResponseHeaders(200, bytes.length);
				try (var os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			} else if (path.equals("/internal/reservations/100/complete")) {
				exchange.sendResponseHeaders(204, -1);
				exchange.close();
			} else {
				exchange.sendResponseHeaders(404, -1);
				exchange.close();
			}
		});
		reservationsServer.start();

		try {
			var req = new MockHttpServletRequest();
			req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ops-jwt");
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

			var principal = new com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal(
					9L, UUID.randomUUID(), "guest@swissotel.com", "ROLE_GUEST", Set.of("service_requests.view"));
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
					principal, "n/a", List.of(new SimpleGrantedAuthority("ROLE_GUEST"))));

			var interceptor = new BearerTokenForwardingInterceptor(new StatelessJwtTokenParser(TEST_SECRET));
			var cfg = new OperationsClientsConfiguration();
			var guestsClient = cfg.guestsInternalClient("http://localhost:" + guestsServer.getAddress().getPort(), interceptor);
			var roomsClient = cfg.roomsInternalClient("http://localhost:" + roomsServer.getAddress().getPort(), interceptor);
			var resClient = cfg.reservationsInternalClient(
					"http://localhost:" + reservationsServer.getAddress().getPort(), interceptor);

			var stayAdapter = new HttpStayCollaboratorsAdapter(resClient, guestsClient, roomsClient);
			var reqAdapter = new HttpServiceRequestCollaboratorsAdapter(mock(StaysContextFacade.class), guestsClient);

			assertEquals(100L, stayAdapter.findReservation("SW26-ABC").orElseThrow().internalId());
			assertEquals(20L, stayAdapter.findGuest(20L).orElseThrow().id());
			assertEquals(30L, stayAdapter.findRoom(30L).orElseThrow().id());
			assertEquals(30L, stayAdapter.findRoom(roomUuid).orElseThrow().id());
			assertEquals(30L, stayAdapter.findAvailableRoom(2L).orElseThrow().id());
			stayAdapter.markRoomOccupied(30L);
			stayAdapter.markRoomCheckedOut(30L);
			stayAdapter.releaseRoom(30L);
			stayAdapter.completeReservation(100L);
			assertTrue(stayAdapter.findReservation("MISSING").isEmpty());
			assertEquals(20L, reqAdapter.currentGuestId().orElseThrow());

			assertTrue(roomCalls.contains("POST /internal/rooms/30/occupy"));
			assertTrue(roomCalls.contains("POST /internal/rooms/30/check-out"));
			assertTrue(roomCalls.contains("POST /internal/rooms/30/release"));
			assertTrue(resCalls.contains("POST /internal/reservations/100/complete"));
			assertTrue(authHeaders.stream().allMatch("Bearer ops-jwt"::equals));
		} finally {
			guestsServer.stop(0);
			roomsServer.stop(0);
			reservationsServer.stop(0);
		}
	}
}
