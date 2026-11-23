package com.swissotel.swissotel360.reservations.infrastructure.http;

import com.sun.net.httpserver.HttpServer;
import com.swissotel.swissotel360.reservations.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.reservations.domain.model.GuestId;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import com.swissotel.swissotel360.reservations.infrastructure.adapter.GuestDirectoryAdapter;
import com.swissotel.swissotel360.reservations.infrastructure.adapter.RoomInventoryAdapter;
import com.swissotel.swissotel360.shared.infrastructure.security.BearerTokenForwardingInterceptor;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessJwtTokenParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationHttpAdaptersTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void resolvesGuestsAndRoomsOverHttpWithForwardedJwtAndTranslates422RateNotFound() throws Exception {
		var guestPublicId = UUID.randomUUID();
		var roomTypePublicId = UUID.randomUUID();
		var lastGuestAuth = new AtomicReference<String>();
		var lastRoomAuth = new AtomicReference<String>();

		var guestsServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		guestsServer.createContext("/", exchange -> {
			lastGuestAuth.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/internal/guests/by-public-id/" + guestPublicId)
					|| path.equals("/internal/guests/10")
					|| path.equals("/internal/guests/by-user-id/5")) {
				byte[] bytes = """
						{"id":10,"publicId":"%s","userId":5,"fullName":"Ada Guest","email":"ada@swissotel.com","documentTypeCode":"DNI","documentNumber":"12345678"}"""
						.formatted(guestPublicId).getBytes(StandardCharsets.UTF_8);
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
			lastRoomAuth.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			String path = exchange.getRequestURI().getPath();
			String query = exchange.getRequestURI().getQuery();
			int status = 404;
			String body = "";
			if (path.equals("/internal/room-types/2")
					|| path.equals("/internal/room-types/by-public-id/" + roomTypePublicId)) {
				status = 200;
				body = """
						{"id":2,"publicId":"%s","name":"Deluxe","capacity":3}""".formatted(roomTypePublicId);
			} else if (path.equals("/internal/room-types/2/sellable-units")) {
				status = 200;
				body = "{\"sellableUnits\":6}";
			} else if (path.equals("/internal/room-types/2/quote")) {
				if (query != null && query.contains("checkIn=2027-12-24")) {
					status = 422;
					body = """
							{"code":"RATE_NOT_FOUND","message":"Rate not found","details":"No rate configured for 2027-12-25"}""";
				} else {
					status = 200;
					body = """
							{"roomTypePublicId":"%s","checkIn":"2026-11-22","checkOut":"2026-11-24","nights":2,"totalAmount":760.00,"breakdown":[{"date":"2026-11-22","price":380.00},{"date":"2026-11-23","price":380.00}]}"""
							.formatted(roomTypePublicId);
				}
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

		try {
			var req = new MockHttpServletRequest();
			req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer res-jwt");
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

			var interceptor = new BearerTokenForwardingInterceptor(new StatelessJwtTokenParser(TEST_SECRET));
			var cfg = new ReservationsClientsConfiguration();
			var guestAdapter = new GuestDirectoryAdapter(
					cfg.guestsInternalClient("http://localhost:" + guestsServer.getAddress().getPort(), interceptor));
			var roomAdapter = new RoomInventoryAdapter(
					cfg.roomsInternalClient("http://localhost:" + roomsServer.getAddress().getPort(), interceptor));

			var foundGuest = guestAdapter.findByPublicId(guestPublicId).orElseThrow();
			assertEquals(10L, foundGuest.id().value());
			assertEquals("Ada Guest", foundGuest.fullName());
			assertEquals("Bearer res-jwt", lastGuestAuth.get());
			assertEquals(10L, guestAdapter.findById(new GuestId(10L)).orElseThrow().id().value());
			assertEquals(10L, guestAdapter.findByUserId(new UserId(5L)).orElseThrow().id().value());
			assertTrue(guestAdapter.findById(new GuestId(999L)).isEmpty());

			var foundRoomType = roomAdapter.findRoomType(roomTypePublicId).orElseThrow();
			assertEquals(2L, foundRoomType.id().value());
			assertEquals("Bearer res-jwt", lastRoomAuth.get());
			assertEquals(2L, roomAdapter.findRoomTypeById(new RoomTypeId(2L)).orElseThrow().id().value());
			assertTrue(roomAdapter.findRoomTypeById(new RoomTypeId(999L)).isEmpty());
			assertEquals(6, roomAdapter.sellableUnits(new RoomTypeId(2L)));

			var quote = roomAdapter.quote(new RoomTypeId(2L),
					new StayPeriod(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 24)));
			assertEquals(new BigDecimal("760.00"), quote.total().amount());
			assertEquals(2, quote.breakdown().size());

			var missingEx = assertThrows(RateNotFoundException.class, () -> roomAdapter.quote(
					new RoomTypeId(2L), new StayPeriod(LocalDate.of(2027, 12, 24), LocalDate.of(2027, 12, 26))));
			assertEquals(LocalDate.of(2027, 12, 25), missingEx.getDate());
		} finally {
			guestsServer.stop(0);
			roomsServer.stop(0);
		}
	}
}
