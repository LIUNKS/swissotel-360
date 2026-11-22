package com.swissotel.swissotel360;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none",
		"reservations.expiration-job.enabled=false",
		"swissotel.messaging.rabbitmq.enabled=false" })
class RbacSecurityIntegrationTest {

	private static final String ADMIN_EMAIL = "admin@swissotel.com";
	private static final String ADMIN_PASSWORD = "Admin123!";
	private static final ZoneId LIMA = ZoneId.of("America/Lima");

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	RbacSecurityIntegrationTest(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void usersGetByIdPreventsIdorAcrossRoles(@LocalServerPort int port) throws Exception {
		var tag = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		var staffEmail = "rbac.staff." + tag + "@swissotel.com";
		var guestEmail = "rbac.guest." + tag + "@swissotel.com";
		List<UUID> createdUsers = new ArrayList<>();

		try (var client = HttpClient.newHttpClient()) {
			var adminToken = signIn(client, port, ADMIN_EMAIL, ADMIN_PASSWORD);

			var adminMeResp = get(client, port, "/api/v1/authentication/me", adminToken);
			assertEquals(200, adminMeResp.statusCode(), adminMeResp.body());
			var adminJson = parseJsonObject(adminMeResp.body());
			assertFalse(adminJson.containsKey("id"), "Public response must not expose internal id: " + adminMeResp.body());
			var adminPublicId = UUID.fromString(JsonPath.parse(adminMeResp.body()).read("$.publicId", String.class));

			var createStaffResp = postJson(client, port, "/api/v1/users", adminToken,
					"""
							{"fullName":"RBAC Staff","email":"%s","password":"StaffPass123!","role":"ROLE_RECEPTIONIST"}
							""".formatted(staffEmail));
			assertEquals(201, createStaffResp.statusCode(), createStaffResp.body());
			assertFalse(parseJsonObject(createStaffResp.body()).containsKey("id"), createStaffResp.body());
			var staffPublicId = UUID.fromString(JsonPath.parse(createStaffResp.body()).read("$.publicId", String.class));
			createdUsers.add(staffPublicId);

			var createGuestUserResp = postJson(client, port, "/api/v1/authentication/sign-up", null,
					"""
							{"fullName":"RBAC Guest","email":"%s","password":"GuestPass123!"}
							""".formatted(guestEmail));
			assertEquals(201, createGuestUserResp.statusCode(), createGuestUserResp.body());
			assertFalse(parseJsonObject(createGuestUserResp.body()).containsKey("id"), createGuestUserResp.body());
			var guestPublicId = UUID
					.fromString(JsonPath.parse(createGuestUserResp.body()).read("$.publicId", String.class));
			createdUsers.add(guestPublicId);

			var staffToken = signIn(client, port, staffEmail, "StaffPass123!");
			var guestToken = signIn(client, port, guestEmail, "GuestPass123!");

			var guestSelfResp = get(client, port, "/api/v1/users/" + guestPublicId, guestToken);
			assertEquals(200, guestSelfResp.statusCode(), guestSelfResp.body());
			assertFalse(parseJsonObject(guestSelfResp.body()).containsKey("id"), guestSelfResp.body());
			assertEquals(guestEmail, JsonPath.parse(guestSelfResp.body()).read("$.email", String.class));

			var guestToAdminResp = get(client, port, "/api/v1/users/" + adminPublicId, guestToken);
			assertEquals(403, guestToAdminResp.statusCode(), guestToAdminResp.body());
			assertEquals("FORBIDDEN", parseJsonObject(guestToAdminResp.body()).get("code"));

			var guestToStaffResp = get(client, port, "/api/v1/users/" + staffPublicId, guestToken);
			assertEquals(403, guestToStaffResp.statusCode(), guestToStaffResp.body());
			assertEquals("FORBIDDEN", parseJsonObject(guestToStaffResp.body()).get("code"));

			var staffToGuestResp = get(client, port, "/api/v1/users/" + guestPublicId, staffToken);
			assertEquals(403, staffToGuestResp.statusCode(), staffToGuestResp.body());
			assertEquals("FORBIDDEN", parseJsonObject(staffToGuestResp.body()).get("code"));

			var staffSelfResp = get(client, port, "/api/v1/users/" + staffPublicId, staffToken);
			assertEquals(200, staffSelfResp.statusCode(), staffSelfResp.body());

			var adminToGuestResp = get(client, port, "/api/v1/users/" + guestPublicId, adminToken);
			assertEquals(200, adminToGuestResp.statusCode(), adminToGuestResp.body());

			var adminNotFoundResp = get(client, port, "/api/v1/users/" + UUID.randomUUID(), adminToken);
			assertEquals(404, adminNotFoundResp.statusCode(), adminNotFoundResp.body());
			assertEquals("USER_NOT_FOUND", parseJsonObject(adminNotFoundResp.body()).get("code"));
		} finally {
			for (var userPublicId : createdUsers) {
				jdbcTemplate.update("DELETE FROM users WHERE public_id = ?", userPublicId);
			}
		}
	}

	@Test
	void guestServiceRequestsEnforceOwnership(@LocalServerPort int port) throws Exception {
		var tag = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		var today = LocalDate.now(LIMA);
		var guestAEmail = "rbac.guesta." + tag + "@swissotel.com";
		var guestBEmail = "rbac.guestb." + tag + "@swissotel.com";
		var docA = "RA" + tag.toUpperCase(Locale.ROOT);
		var docB = "RB" + tag.toUpperCase(Locale.ROOT);

		List<UUID> createdUsers = new ArrayList<>();
		List<UUID> createdGuests = new ArrayList<>();
		List<UUID> createdRoomTypes = new ArrayList<>();
		List<UUID> createdRooms = new ArrayList<>();
		List<UUID> createdReservations = new ArrayList<>();
		List<UUID> createdStays = new ArrayList<>();
		List<UUID> createdServiceRequests = new ArrayList<>();

		try (var client = HttpClient.newHttpClient()) {
			var adminToken = signIn(client, port, ADMIN_EMAIL, ADMIN_PASSWORD);

			var docTypesResp = get(client, port, "/api/v1/document-types", adminToken);
			assertEquals(200, docTypesResp.statusCode(), docTypesResp.body());
			String passportTypeCode = null;
			List<Map<String, Object>> docTypes = JsonPath.parse(docTypesResp.body()).read("$");
			for (var dt : docTypes) {
				assertFalse(dt.containsKey("id"), "DocumentTypeResource must not expose internal id: " + dt);
				if ("PASSPORT".equals(dt.get("code"))) {
					passportTypeCode = (String) dt.get("code");
				}
			}
			assertNotNull(passportTypeCode, "PASSPORT document type must exist");

			var roomTypeResp = postJson(client, port, "/api/v1/room-types", adminToken,
					"""
							{"name":"RBACSuite-%s","capacity":2,"description":"RBAC test"}
							""".formatted(tag));
			assertEquals(201, roomTypeResp.statusCode(), roomTypeResp.body());
			var roomTypeId = UUID.fromString(JsonPath.parse(roomTypeResp.body()).read("$.publicId", String.class));
			createdRoomTypes.add(roomTypeId);

			var rateResp = postJson(client, port, "/api/v1/room-types/" + roomTypeId + "/rates", adminToken,
					"""
							{"price":280.00,"season":"rbac","validFrom":"%s","validTo":"%s"}
							""".formatted(today, today.plusDays(10)));
			assertEquals(201, rateResp.statusCode(), rateResp.body());
			assertFalse(parseJsonObject(rateResp.body()).containsKey("id"), rateResp.body());
			assertNotNull(JsonPath.parse(rateResp.body()).read("$.publicId", String.class));

			var roomResp = postJson(client, port, "/api/v1/rooms", adminToken,
					"""
							{"roomTypePublicId":"%s","roomNumber":"R%s","floor":4}
							""".formatted(roomTypeId, tag.substring(0, 6)));
			assertEquals(201, roomResp.statusCode(), roomResp.body());
			var roomId = UUID.fromString(JsonPath.parse(roomResp.body()).read("$.publicId", String.class));
			createdRooms.add(roomId);

			var userAResp = postJson(client, port, "/api/v1/authentication/sign-up", null,
					"""
							{"fullName":"Guest A","email":"%s","password":"GuestPass123!"}
							""".formatted(guestAEmail));
			assertEquals(201, userAResp.statusCode(), userAResp.body());
			var userAId = UUID.fromString(JsonPath.parse(userAResp.body()).read("$.publicId", String.class));
			createdUsers.add(userAId);

			var userBResp = postJson(client, port, "/api/v1/authentication/sign-up", null,
					"""
							{"fullName":"Guest B","email":"%s","password":"GuestPass123!"}
							""".formatted(guestBEmail));
			assertEquals(201, userBResp.statusCode(), userBResp.body());
			var userBId = UUID.fromString(JsonPath.parse(userBResp.body()).read("$.publicId", String.class));
			createdUsers.add(userBId);

			var guestAResp = postJson(client, port, "/api/v1/guests", adminToken,
					"""
							{"documentTypeCode":"%s","documentNumber":"%s","firstName":"Guest","lastName":"Alpha","email":"%s","phone":"+51999111001","countryOfOrigin":"Peru"}
							"""
							.formatted(passportTypeCode, docA, guestAEmail));
			assertEquals(201, guestAResp.statusCode(), guestAResp.body());
			var guestAId = UUID.fromString(JsonPath.parse(guestAResp.body()).read("$.publicId", String.class));
			createdGuests.add(guestAId);

			var searchGuestsResp = get(client, port, "/api/v1/guests?q=Alpha", adminToken);
			assertEquals(200, searchGuestsResp.statusCode(), searchGuestsResp.body());
			assertTrue(searchGuestsResp.body().contains(guestAId.toString()), searchGuestsResp.body());

			var getGuestAResp = get(client, port, "/api/v1/guests/" + guestAId, adminToken);
			assertEquals(200, getGuestAResp.statusCode(), getGuestAResp.body());

			var guestBResp = postJson(client, port, "/api/v1/guests", adminToken,
					"""
							{"documentTypeCode":"%s","documentNumber":"%s","firstName":"Guest","lastName":"Beta","email":"%s","phone":"+51999111002","countryOfOrigin":"Peru"}
							"""
							.formatted(passportTypeCode, docB, guestBEmail));
			assertEquals(201, guestBResp.statusCode(), guestBResp.body());
			var guestBId = UUID.fromString(JsonPath.parse(guestBResp.body()).read("$.publicId", String.class));
			createdGuests.add(guestBId);

			assertEquals(200, postJson(client, port, "/api/v1/guests/" + guestAId + "/link-account", adminToken,
					"""
							{"userPublicId":"%s"}
							""".formatted(userAId)).statusCode());
			assertEquals(200, postJson(client, port, "/api/v1/guests/" + guestBId + "/link-account", adminToken,
					"""
							{"userPublicId":"%s"}
							""".formatted(userBId)).statusCode());

			var resResp = postJson(client, port, "/api/v1/reservations", adminToken,
					"""
							{"guestPublicId":"%s","roomTypePublicId":"%s","checkInDate":"%s","checkOutDate":"%s","paymentType":"deferred"}
							"""
							.formatted(guestAId, roomTypeId, today, today.plusDays(2)));
			assertEquals(201, resResp.statusCode(), resResp.body());
			var resPublicId = UUID.fromString(JsonPath.parse(resResp.body()).read("$.publicId", String.class));
			var resCode = JsonPath.parse(resResp.body()).read("$.reservationCode", String.class);
			createdReservations.add(resPublicId);

			var checkInResp = postJson(client, port, "/api/v1/stays/check-in", adminToken,
					"""
							{"reservationCode":"%s","documentType":"PASSPORT","documentNumber":"%s","roomId":"%s"}
							""".formatted(resCode, docA, roomId));
			assertEquals(201, checkInResp.statusCode(), checkInResp.body());
			var stayAId = UUID.fromString(JsonPath.parse(checkInResp.body()).read("$.stayId", String.class));
			createdStays.add(stayAId);

			var req1Resp = postJson(client, port, "/api/v1/service-requests", adminToken,
					"""
							{"stayId":"%s","serviceTypeCode":"room_service","description":"Admin coffee"}
							""".formatted(stayAId));
			assertEquals(201, req1Resp.statusCode(), req1Resp.body());
			var req1Id = UUID.fromString(JsonPath.parse(req1Resp.body()).read("$.requestId", String.class));
			createdServiceRequests.add(req1Id);

			// Guest A (owner) can list, view, and create on Stay A
			var guestAToken = signIn(client, port, guestAEmail, "GuestPass123!");
			var guestAListResp = get(client, port, "/api/v1/service-requests", guestAToken);
			assertEquals(200, guestAListResp.statusCode(), guestAListResp.body());
			assertTrue(guestAListResp.body().contains(req1Id.toString()), guestAListResp.body());

			var guestAGetResp = get(client, port, "/api/v1/service-requests/" + req1Id, guestAToken);
			assertEquals(200, guestAGetResp.statusCode(), guestAGetResp.body());

			var guestACreateResp = postJson(client, port, "/api/v1/service-requests", guestAToken,
					"""
							{"stayId":"%s","serviceTypeCode":"room_service","description":"Guest A tea"}
							""".formatted(stayAId));
			assertEquals(201, guestACreateResp.statusCode(), guestACreateResp.body());
			var req2Id = UUID.fromString(JsonPath.parse(guestACreateResp.body()).read("$.requestId", String.class));
			createdServiceRequests.add(req2Id);

			// Guest B (non-owner) cannot see S1/S2, filter by Stay A, or create on Stay A
			var guestBToken = signIn(client, port, guestBEmail, "GuestPass123!");
			var guestBListResp = get(client, port, "/api/v1/service-requests", guestBToken);
			assertEquals(200, guestBListResp.statusCode(), guestBListResp.body());
			assertFalse(guestBListResp.body().contains(req1Id.toString()), guestBListResp.body());
			assertFalse(guestBListResp.body().contains(req2Id.toString()), guestBListResp.body());

			var guestBGetResp = get(client, port, "/api/v1/service-requests/" + req1Id, guestBToken);
			assertEquals(404, guestBGetResp.statusCode(), guestBGetResp.body());

			var guestBFilterResp = get(client, port, "/api/v1/service-requests?stayId=" + stayAId, guestBToken);
			assertEquals(404, guestBFilterResp.statusCode(), guestBFilterResp.body());

			var guestBCreateResp = postJson(client, port, "/api/v1/service-requests", guestBToken,
					"""
							{"stayId":"%s","serviceTypeCode":"room_service","description":"Intruder coffee"}
							""".formatted(stayAId));
			assertEquals(404, guestBCreateResp.statusCode(), guestBCreateResp.body());
		} finally {
			for (var stayId : createdStays) {
				jdbcTemplate.update(
						"DELETE FROM charges WHERE folio_id IN (SELECT id FROM folios WHERE stay_id = (SELECT id FROM stays WHERE public_id = ?))",
						stayId);
				jdbcTemplate.update(
						"DELETE FROM folios WHERE stay_id = (SELECT id FROM stays WHERE public_id = ?)",
						stayId);
			}
			for (var reqId : createdServiceRequests) {
				jdbcTemplate.update("DELETE FROM service_requests WHERE public_id = ?", reqId);
			}
			for (var stayId : createdStays) {
				jdbcTemplate.update(
						"DELETE FROM digital_keys WHERE stay_id = (SELECT id FROM stays WHERE public_id = ?)",
						stayId);
				jdbcTemplate.update("DELETE FROM stays WHERE public_id = ?", stayId);
			}
			for (var resId : createdReservations) {
				jdbcTemplate.update("DELETE FROM reservations WHERE public_id = ?", resId);
			}
			for (var guestId : createdGuests) {
				jdbcTemplate.update("DELETE FROM guests WHERE public_id = ?", guestId);
			}
			for (var roomId : createdRooms) {
				jdbcTemplate.update("DELETE FROM rooms WHERE public_id = ?", roomId);
			}
			for (var roomTypeId : createdRoomTypes) {
				jdbcTemplate.update(
						"DELETE FROM rates WHERE room_type_id = (SELECT id FROM room_types WHERE public_id = ?)",
						roomTypeId);
				jdbcTemplate.update("DELETE FROM room_types WHERE public_id = ?", roomTypeId);
			}
			for (var userId : createdUsers) {
				jdbcTemplate.update("DELETE FROM users WHERE public_id = ?", userId);
			}
		}
	}

	@Test
	void sessionEndpointsMeSignOutAndCookieFlags(@LocalServerPort int port) throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var unauthMeResp = get(client, port, "/api/v1/authentication/me", null);
			assertEquals(401, unauthMeResp.statusCode(), unauthMeResp.body());
			assertEquals("UNAUTHORIZED", parseJsonObject(unauthMeResp.body()).get("code"));

			var signInResp = postJson(client, port, "/api/v1/authentication/sign-in", null,
					"""
							{"email":"%s","password":"%s"}
							""".formatted(ADMIN_EMAIL, ADMIN_PASSWORD));
			assertEquals(200, signInResp.statusCode(), signInResp.body());
			var signInSetCookie = String.join("; ", signInResp.headers().allValues("Set-Cookie"));
			assertTrue(signInSetCookie.contains("HttpOnly"), signInSetCookie);
			assertTrue(signInSetCookie.contains("SameSite=Lax"), signInSetCookie);
			var adminToken = extractTokenCookie(signInResp);

			var authMeResp = get(client, port, "/api/v1/authentication/me", adminToken);
			assertEquals(200, authMeResp.statusCode(), authMeResp.body());
			assertEquals(ADMIN_EMAIL, JsonPath.parse(authMeResp.body()).read("$.email", String.class));
			assertEquals("ROLE_SUPER_ADMIN", JsonPath.parse(authMeResp.body()).read("$.role", String.class));

			var signOutResp = postJson(client, port, "/api/v1/authentication/sign-out", adminToken, "{}");
			assertEquals(204, signOutResp.statusCode(), signOutResp.body());
			var signOutCookie = String.join("; ", signOutResp.headers().allValues("Set-Cookie"));
			assertTrue(signOutCookie.contains("Max-Age=0"), signOutCookie);
			assertTrue(signOutCookie.contains("HttpOnly"), signOutCookie);
			assertTrue(signOutCookie.contains("SameSite=Lax"), signOutCookie);
		}
	}

	private static String signIn(HttpClient client, int port, String email, String password) throws Exception {
		var response = postJson(client, port, "/api/v1/authentication/sign-in", null,
				"""
						{"email":"%s","password":"%s"}
						""".formatted(email, password));
		assertEquals(200, response.statusCode(), "Sign-in failed for " + email + ": " + response.body());
		return extractTokenCookie(response);
	}

	private static HttpResponse<String> get(HttpClient client, int port, String path, String tokenCookie)
			throws Exception {
		var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
		if (tokenCookie != null) {
			builder.header("Cookie", "token=" + tokenCookie);
		}
		return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static HttpResponse<String> postJson(HttpClient client, int port, String path, String tokenCookie,
			String jsonBody) throws Exception {
		var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
		if (tokenCookie != null) {
			builder.header("Cookie", "token=" + tokenCookie);
		}
		return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static String extractTokenCookie(HttpResponse<?> response) {
		return response.headers().allValues("Set-Cookie").stream()
				.filter(header -> header.startsWith("token="))
				.map(header -> {
					var valuePart = header.substring("token=".length());
					var semicolonIdx = valuePart.indexOf(';');
					return semicolonIdx >= 0 ? valuePart.substring(0, semicolonIdx) : valuePart;
				})
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing 'token' Set-Cookie header"));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseJsonObject(String body) {
		assertFalse(body == null || body.isBlank(), "Expected non-empty JSON response body");
		return JsonPath.parse(body).read("$", Map.class);
	}
}
