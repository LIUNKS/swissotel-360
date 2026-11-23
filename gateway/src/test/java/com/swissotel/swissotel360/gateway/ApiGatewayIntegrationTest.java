package com.swissotel.swissotel360.gateway;

import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayIntegrationTest {

	private static HttpServer iamStub;
	private static HttpServer guestsStub;
	private static HttpServer roomsStub;
	private static HttpServer reservationsStub;
	private static HttpServer operationsStub;
	private static HttpServer billingStub;

	private static final List<RecordedRequest> iamRequests = new CopyOnWriteArrayList<>();
	private static final List<RecordedRequest> guestsRequests = new CopyOnWriteArrayList<>();
	private static final List<RecordedRequest> roomsRequests = new CopyOnWriteArrayList<>();
	private static final List<RecordedRequest> reservationsRequests = new CopyOnWriteArrayList<>();
	private static final List<RecordedRequest> operationsRequests = new CopyOnWriteArrayList<>();
	private static final List<RecordedRequest> billingRequests = new CopyOnWriteArrayList<>();

	private static List<List<RecordedRequest>> allLogs() {
		return List.of(iamRequests, guestsRequests, roomsRequests, reservationsRequests, operationsRequests,
				billingRequests);
	}

	@BeforeAll
	static void startStubs() throws IOException {
		iamStub = startStub("iam", iamRequests);
		guestsStub = startStub("guests", guestsRequests);
		roomsStub = startStub("rooms", roomsRequests);
		reservationsStub = startStub("reservations", reservationsRequests);
		operationsStub = startStub("operations", operationsRequests);
		billingStub = startStub("billing", billingRequests);
	}

	@AfterAll
	static void stopStubs() {
		for (var stub : List.of(iamStub, guestsStub, roomsStub, reservationsStub, operationsStub, billingStub)) {
			if (stub != null) {
				stub.stop(0);
			}
		}
	}

	@BeforeEach
	void clearRecordedRequests() {
		allLogs().forEach(List::clear);
	}

	@DynamicPropertySource
	static void overrideDownstreamUrls(DynamicPropertyRegistry registry) {
		registry.add("gateway.routes.iam-service-url", () -> "http://localhost:" + iamStub.getAddress().getPort());
		registry.add("gateway.routes.guests-service-url", () -> "http://localhost:" + guestsStub.getAddress().getPort());
		registry.add("gateway.routes.rooms-service-url", () -> "http://localhost:" + roomsStub.getAddress().getPort());
		registry.add("gateway.routes.reservations-service-url",
				() -> "http://localhost:" + reservationsStub.getAddress().getPort());
		registry.add("gateway.routes.operations-service-url",
				() -> "http://localhost:" + operationsStub.getAddress().getPort());
		registry.add("gateway.routes.billing-service-url",
				() -> "http://localhost:" + billingStub.getAddress().getPort());
	}

	@Test
	void routesAllSixServicePathGroupsAndPreservesAuthHeaders(@LocalServerPort int port) throws Exception {
		var uuid = UUID.randomUUID();
		var expectedRoutes = Map.of(
				"iam", List.of("/api/v1/authentication/me", "/api/v1/users/" + uuid),
				"guests", List.of("/api/v1/guests", "/api/v1/document-types"),
				"rooms", List.of("/api/v1/rooms", "/api/v1/room-types/" + uuid + "/rates",
						"/api/v1/availability?checkIn=2026-11-01&checkOut=2026-11-03"),
				"reservations", List.of("/api/v1/reservations", "/api/v1/reservations/" + uuid),
				"operations", List.of("/api/v1/stays", "/api/v1/digital-keys", "/api/v1/service-requests",
						"/api/v1/service-types"),
				"billing", List.of("/api/v1/folios?stayId=" + uuid, "/api/v1/payment-methods",
						"/api/v1/payments", "/api/v1/invoices"));

		try (var client = HttpClient.newHttpClient()) {
			for (var entry : expectedRoutes.entrySet()) {
				String service = entry.getKey();
				for (var path : entry.getValue()) {
					var req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
							.header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
							.header(HttpHeaders.COOKIE, "token=cookie-jwt")
							.GET()
							.build();
					var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
					assertEquals(200, resp.statusCode(), "Expected 200 for " + path + ": " + resp.body());
					assertEquals(service, JsonPath.parse(resp.body()).read("$.service", String.class));
				}
			}
		}

		assertEquals(2, iamRequests.size());
		assertEquals(2, guestsRequests.size());
		assertEquals(3, roomsRequests.size());
		assertEquals(2, reservationsRequests.size());
		assertEquals(4, operationsRequests.size());
		assertEquals(4, billingRequests.size());

		for (var log : allLogs()) {
			for (var recorded : log) {
				assertEquals("Bearer test-jwt", recorded.authorization());
				assertTrue(recorded.cookie() != null && recorded.cookie().contains("token=cookie-jwt"));
			}
		}
	}

	@Test
	void blocksInternalEndpointsAtGatewayEdgeWithoutCallingAnyDownstream(@LocalServerPort int port) throws Exception {
		var blockedPaths = List.of(
				"/internal/users/1",
				"/internal/guests/1",
				"/internal/rooms/1",
				"/internal/reservations/by-code/SW26-123",
				"/internal/stays/1",
				"/internal/service-requests/1",
				"/internal",
				"//internal/stays/1");

		try (var client = HttpClient.newHttpClient()) {
			for (var path : blockedPaths) {
				var req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
				var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
				assertEquals(403, resp.statusCode(), "Expected 403 for " + path + " but got " + resp.statusCode());
				assertEquals("FORBIDDEN", JsonPath.parse(resp.body()).read("$.code", String.class));
				assertFalse(resp.body().isBlank());
			}
		}

		for (var log : allLogs()) {
			assertEquals(0, log.size());
		}
	}

	@Test
	void rewritesAllSixOpenApiDocsAggregatesSwaggerConfigAndExposesHealth(@LocalServerPort int port) throws Exception {
		var services = List.of("iam", "guests", "rooms", "reservations", "operations", "billing");

		try (var client = HttpClient.newHttpClient()) {
			for (var service : services) {
				var docs = client.send(
						HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs/" + service))
								.GET().build(),
						HttpResponse.BodyHandlers.ofString());
				assertEquals(200, docs.statusCode(), docs.body());
				assertEquals("3.0.1", JsonPath.parse(docs.body()).read("$.openapi", String.class));
				assertEquals(service, JsonPath.parse(docs.body()).read("$.info.title", String.class));
			}

			var swaggerConfig = client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs/swagger-config"))
							.GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, swaggerConfig.statusCode(), swaggerConfig.body());
			List<String> urls = JsonPath.parse(swaggerConfig.body()).read("$.urls[*].url");
			assertEquals(6, urls.size());
			for (var service : services) {
				assertTrue(urls.contains("/v3/api-docs/" + service), swaggerConfig.body());
			}

			var health = client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health")).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, health.statusCode(), health.body());
			assertEquals("UP", JsonPath.parse(health.body()).read("$.status", String.class));
		}

		for (var log : allLogs()) {
			assertEquals(1, log.size());
			assertEquals("/v3/api-docs", log.getFirst().path());
		}
	}

	@Test
	void appliesCentralizedCorsAndDeduplicatesUpstreamCorsHeaders(@LocalServerPort int port) throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var preflight = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/folios"))
					.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
					.header(HttpHeaders.ORIGIN, "http://localhost:4200")
					.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
					.build();
			var preflightResp = client.send(preflight, HttpResponse.BodyHandlers.discarding());
			assertEquals(200, preflightResp.statusCode());
			assertEquals(List.of("http://localhost:4200"),
					preflightResp.headers().allValues(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
			assertEquals(List.of("true"),
					preflightResp.headers().allValues(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));

			var proxied = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/rooms"))
					.header(HttpHeaders.ORIGIN, "http://localhost:4200")
					.GET()
					.build();
			var proxiedResp = client.send(proxied, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, proxiedResp.statusCode());
			assertEquals(List.of("http://localhost:4200"),
					proxiedResp.headers().allValues(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
			assertEquals(List.of("true"),
					proxiedResp.headers().allValues(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
		}
	}

	private static HttpServer startStub(String serviceName, List<RecordedRequest> log) throws IOException {
		var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			String auth = exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			String cookie = exchange.getRequestHeaders().getFirst(HttpHeaders.COOKIE);
			log.add(new RecordedRequest(exchange.getRequestMethod(), path, auth, cookie));

			byte[] body = "/v3/api-docs".equals(path)
					? """
							{"openapi":"3.0.1","info":{"title":"%s","version":"1.0.0"},"paths":{}}"""
							.formatted(serviceName).getBytes(StandardCharsets.UTF_8)
					: """
							{"service":"%s","path":"%s"}""".formatted(serviceName, path)
							.getBytes(StandardCharsets.UTF_8);

			var headers = exchange.getResponseHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
			headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://downstream-should-be-stripped.local");
			headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			exchange.sendResponseHeaders(200, body.length);
			try (var os = exchange.getResponseBody()) {
				os.write(body);
			}
		});
		server.start();
		return server;
	}

	private record RecordedRequest(String method, String path, String authorization, String cookie) {
	}
}
