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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none",
		"reservations.expiration-job.enabled=false" })
class BackendFixesIntegrationTest {

	private static final String ADMIN_EMAIL = "admin@swissotel.com";
	private static final String ADMIN_PASSWORD = "Admin123!";

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	BackendFixesIntegrationTest(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void loginReturnsUniform401WithoutUserEnumerationAnd200WithTokenCookie(@LocalServerPort int port)
			throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var missingUserResponse = postJson(client, port, "/api/v1/authentication/sign-in", null,
					"""
							{"email":"nonexistent.user@swissotel.com","password":"WrongPassword123!"}
							""");
			var wrongPasswordResponse = postJson(client, port, "/api/v1/authentication/sign-in", null,
					"""
							{"email":"%s","password":"WrongPassword123!"}
							""".formatted(ADMIN_EMAIL));

			assertEquals(401, missingUserResponse.statusCode());
			assertEquals(401, wrongPasswordResponse.statusCode());

			var missingJson = parseJsonObject(missingUserResponse.body());
			var wrongPwdJson = parseJsonObject(wrongPasswordResponse.body());

			assertEquals("UNAUTHORIZED", missingJson.get("code"));
			assertEquals(missingJson.get("code"), wrongPwdJson.get("code"));
			assertEquals(missingJson.get("message"), wrongPwdJson.get("message"));
			assertEquals(missingJson.get("details"), wrongPwdJson.get("details"));
			assertNotNull(missingJson.get("message"));
			assertFalse(missingJson.containsKey("error"));

			var unauthResponse = get(client, port, "/api/rooms", null);
			assertEquals(401, unauthResponse.statusCode());
			var unauthJson = parseJsonObject(unauthResponse.body());
			assertEquals("UNAUTHORIZED", unauthJson.get("code"));
			assertEquals("Authentication required", unauthJson.get("message"));
			assertFalse(unauthJson.containsKey("error"));

			var adminLoginResponse = postJson(client, port, "/api/v1/authentication/sign-in", null,
					"""
							{"email":"%s","password":"%s"}
							""".formatted(ADMIN_EMAIL, ADMIN_PASSWORD));
			assertEquals(200, adminLoginResponse.statusCode());
			var tokenCookie = extractTokenCookie(adminLoginResponse);
			assertFalse(tokenCookie.isBlank(), "Expected non-empty token cookie on successful sign-in");
		}
	}

	@Test
	void reservationsSearchWorksForAllFilterCombinationsAgainstPostgres(@LocalServerPort int port) throws Exception {
		UUID createdGuestPublicId = null;
		try (var client = HttpClient.newHttpClient()) {
			var token = signInAsAdmin(client, port);

			var existingGuests = jdbcTemplate.queryForList("SELECT public_id FROM guests LIMIT 1", UUID.class);
			UUID guestPublicId;
			if (existingGuests.isEmpty()) {
				createdGuestPublicId = UUID.randomUUID();
				var docTypeId = jdbcTemplate.queryForObject("SELECT id FROM document_types LIMIT 1", Long.class);
				assertNotNull(docTypeId, "Expected seeded document_types row in PostgreSQL");
				var docNumber = "T" + createdGuestPublicId.toString().replace("-", "").substring(0, 12);
				jdbcTemplate.update(
						"INSERT INTO guests (public_id, document_type_id, document_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
						createdGuestPublicId, docTypeId, docNumber, "Test", "Guest");
				guestPublicId = createdGuestPublicId;
			} else {
				guestPublicId = existingGuests.getFirst();
			}

			var paths = List.of(
					"/api/reservations?page=0&size=5",
					"/api/reservations?status=pending",
					"/api/reservations?guestPublicId=" + guestPublicId,
					"/api/reservations?checkInFrom=2026-01-01&checkInTo=2026-12-31",
					"/api/reservations?code=SW");

			for (var path : paths) {
				var response = get(client, port, path, token);
				assertEquals(200, response.statusCode(),
						"Expected 200 for " + path + " but got " + response.statusCode() + ": " + response.body());
			}
		} finally {
			if (createdGuestPublicId != null) {
				jdbcTemplate.update("DELETE FROM guests WHERE public_id = ?", createdGuestPublicId);
			}
		}
	}

	@Test
	void roomRatesCreateHandlesValidToOverlapAndOpenEndedAgainstPostgres(@LocalServerPort int port)
			throws Exception {
		UUID createdRoomTypePublicId = null;
		try (var client = HttpClient.newHttpClient()) {
			var token = signInAsAdmin(client, port);

			var roomTypeName = "VerifSuite-" + UUID.randomUUID().toString().substring(0, 8);
			var createTypeResponse = postJson(client, port, "/api/room-types", token,
					"""
							{"name":"%s","capacity":2,"description":"Temporary test room type"}
							""".formatted(roomTypeName));
			assertEquals(201, createTypeResponse.statusCode(), createTypeResponse.body());
			createdRoomTypePublicId = UUID.fromString(
					JsonPath.parse(createTypeResponse.body()).read("$.publicId", String.class));

			var rateBasePath = "/api/room-types/" + createdRoomTypePublicId + "/rates";

			var closedRateResponse = postJson(client, port, rateBasePath, token,
					"""
							{"price":350.00,"season":"test-closed","validFrom":"2027-01-01","validTo":"2027-01-31"}
							""");
			assertEquals(201, closedRateResponse.statusCode(), closedRateResponse.body());

			var overlapResponse = postJson(client, port, rateBasePath, token,
					"""
							{"price":390.00,"season":"test-overlap","validFrom":"2027-01-15","validTo":"2027-02-15"}
							""");
			assertEquals(409, overlapResponse.statusCode(), overlapResponse.body());
			var overlapJson = parseJsonObject(overlapResponse.body());
			assertEquals("RATE_OVERLAP", overlapJson.get("code"));
			assertNotNull(overlapJson.get("message"));
			assertFalse(overlapJson.containsKey("error"));

			var openEndedRateResponse = postJson(client, port, rateBasePath, token,
					"""
							{"price":420.00,"season":"test-open","validFrom":"2027-02-01","validTo":null}
							""");
			assertEquals(201, openEndedRateResponse.statusCode(), openEndedRateResponse.body());
		} finally {
			if (createdRoomTypePublicId != null) {
				jdbcTemplate.update(
						"DELETE FROM rates WHERE room_type_id = (SELECT id FROM room_types WHERE public_id = ?)",
						createdRoomTypePublicId);
				jdbcTemplate.update("DELETE FROM room_types WHERE public_id = ?", createdRoomTypePublicId);
			}
		}
	}

	@Test
	void roomsListWorksForAllFilterCombinationsAgainstPostgres(@LocalServerPort int port) throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var token = signInAsAdmin(client, port);
			var paths = List.of(
					"/api/rooms",
					"/api/rooms?status=AVAILABLE",
					"/api/rooms?roomTypeId=1",
					"/api/rooms?roomTypeId=1&status=AVAILABLE");

			for (var path : paths) {
				var response = get(client, port, path, token);
				assertEquals(200, response.statusCode(),
						"Expected 200 for " + path + " but got " + response.statusCode() + ": " + response.body());
			}
		}
	}

	@Test
	void errorResponsesFollowUnifiedErrorResourceWithoutInternalLeakage(@LocalServerPort int port) throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var token = signInAsAdmin(client, port);

			for (var invalidStatusPath : List.of("/api/reservations?status=zzz", "/api/rooms?status=zzz")) {
				var response = get(client, port, invalidStatusPath, token);
				assertEquals(400, response.statusCode(), response.body());
				var json = parseJsonObject(response.body());
				assertNotNull(json.get("code"));
				assertNotNull(json.get("message"));
				assertFalse(json.containsKey("error"));
				assertFalse(response.body().contains("com.swissotel"), "Leaked package in: " + response.body());
				assertFalse(response.body().contains("java."), "Leaked java.* in: " + response.body());
				assertFalse(response.body().contains("org.springframework"),
						"Leaked spring package in: " + response.body());
			}

			var userNotFound = get(client, port, "/api/v1/users/99999999", token);
			assertEquals(404, userNotFound.statusCode(), userNotFound.body());
			assertFalse(userNotFound.body().isBlank());
			var userJson = parseJsonObject(userNotFound.body());
			assertEquals("USER_NOT_FOUND", userJson.get("code"));
			assertNotNull(userJson.get("message"));
			assertFalse(userJson.containsKey("error"));

			var roomTypeNotFound = get(client, port, "/api/room-types/" + UUID.randomUUID(), token);
			assertEquals(404, roomTypeNotFound.statusCode(), roomTypeNotFound.body());
			var roomTypeJson = parseJsonObject(roomTypeNotFound.body());
			assertEquals("ROOM_TYPE_NOT_FOUND", roomTypeJson.get("code"));
			assertNotNull(roomTypeJson.get("message"));
			assertFalse(roomTypeJson.containsKey("error"));
		}
	}

	private static String signInAsAdmin(HttpClient client, int port) throws Exception {
		var response = postJson(client, port, "/api/v1/authentication/sign-in", null,
				"""
						{"email":"%s","password":"%s"}
						""".formatted(ADMIN_EMAIL, ADMIN_PASSWORD));
		assertEquals(200, response.statusCode(), "Admin login failed: " + response.body());
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
