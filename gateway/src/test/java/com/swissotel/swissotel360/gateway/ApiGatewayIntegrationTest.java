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
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayIntegrationTest {

	private static HttpServer billingStub;
	private static HttpServer monolithStub;
	private static int billingPort;
	private static int monolithPort;

	private static final List<RecordedRequest> billingRequests = new CopyOnWriteArrayList<>();
	private static final List<RecordedRequest> monolithRequests = new CopyOnWriteArrayList<>();

	@BeforeAll
	static void startStubs() throws IOException {
		billingStub = startStub("billing", billingRequests);
		monolithStub = startStub("monolith", monolithRequests);
		billingPort = billingStub.getAddress().getPort();
		monolithPort = monolithStub.getAddress().getPort();
	}

	@AfterAll
	static void stopStubs() {
		if (billingStub != null) {
			billingStub.stop(0);
		}
		if (monolithStub != null) {
			monolithStub.stop(0);
		}
	}

	@BeforeEach
	void clearRecordedRequests() {
		billingRequests.clear();
		monolithRequests.clear();
	}

	@DynamicPropertySource
	static void overrideDownstreamUrls(DynamicPropertyRegistry registry) {
		registry.add("gateway.routes.billing-service-url", () -> "http://localhost:" + billingPort);
		registry.add("gateway.routes.monolith-url", () -> "http://localhost:" + monolithPort);
	}

	@Test
	void routesBillingPathsToBillingServiceAndPreservesAuthHeaders(@LocalServerPort int port) throws Exception {
		var uuid = UUID.randomUUID();
		var paths = List.of(
				"/api/v1/folios?stayId=" + uuid,
				"/api/v1/folios/" + uuid + "/summary",
				"/api/v1/payment-methods",
				"/api/v1/payments",
				"/api/v1/invoices");

		try (var client = HttpClient.newHttpClient()) {
			for (var path : paths) {
				var req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
						.header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
						.header(HttpHeaders.COOKIE, "token=cookie-jwt")
						.GET()
						.build();
				var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
				assertEquals(200, resp.statusCode(), "Expected 200 for " + path + ": " + resp.body());
				assertEquals("billing", JsonPath.parse(resp.body()).read("$.service", String.class));
			}
		}

		assertEquals(paths.size(), billingRequests.size());
		assertEquals(0, monolithRequests.size());
		for (var recorded : billingRequests) {
			assertEquals("Bearer test-jwt", recorded.authorization());
			assertTrue(recorded.cookie() != null && recorded.cookie().contains("token=cookie-jwt"));
		}
	}

	@Test
	void routesOtherBoundedContextsToMonolith(@LocalServerPort int port) throws Exception {
		var uuid = UUID.randomUUID();
		var paths = List.of(
				"/api/v1/authentication/me",
				"/api/v1/users/" + uuid,
				"/api/v1/guests",
				"/api/v1/document-types",
				"/api/v1/rooms",
				"/api/v1/room-types/" + uuid + "/rates",
				"/api/v1/availability?checkIn=2026-11-01&checkOut=2026-11-03",
				"/api/v1/reservations",
				"/api/v1/stays",
				"/api/v1/digital-keys",
				"/api/v1/service-requests",
				"/api/v1/service-types");

		try (var client = HttpClient.newHttpClient()) {
			for (var path : paths) {
				var req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
				var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
				assertEquals(200, resp.statusCode(), "Expected 200 for " + path + ": " + resp.body());
				assertEquals("monolith", JsonPath.parse(resp.body()).read("$.service", String.class));
			}
		}

		assertEquals(paths.size(), monolithRequests.size());
		assertEquals(0, billingRequests.size());
	}

	@Test
	void blocksInternalEndpointsAtGatewayEdgeWithoutCallingDownstream(@LocalServerPort int port) throws Exception {
		var blockedPaths = List.of(
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

		assertEquals(0, billingRequests.size());
		assertEquals(0, monolithRequests.size());
	}

	@Test
	void rewritesOpenApiDocsAggregatesSwaggerConfigAndExposesHealth(@LocalServerPort int port) throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var billingDocs = client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs/billing")).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, billingDocs.statusCode());
			assertEquals("3.0.1", JsonPath.parse(billingDocs.body()).read("$.openapi", String.class));
			assertEquals("billing", JsonPath.parse(billingDocs.body()).read("$.info.title", String.class));

			var monolithDocs = client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs/monolith")).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, monolithDocs.statusCode());
			assertEquals("3.0.1", JsonPath.parse(monolithDocs.body()).read("$.openapi", String.class));
			assertEquals("monolith", JsonPath.parse(monolithDocs.body()).read("$.info.title", String.class));

			var swaggerConfig = client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs/swagger-config"))
							.GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, swaggerConfig.statusCode(), swaggerConfig.body());
			List<String> urls = JsonPath.parse(swaggerConfig.body()).read("$.urls[*].url");
			assertTrue(urls.contains("/v3/api-docs/monolith"), swaggerConfig.body());
			assertTrue(urls.contains("/v3/api-docs/billing"), swaggerConfig.body());

			var health = client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health")).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, health.statusCode(), health.body());
			assertEquals("UP", JsonPath.parse(health.body()).read("$.status", String.class));
		}

		assertEquals(1, billingRequests.size());
		assertEquals("/v3/api-docs", billingRequests.getFirst().path());
		assertEquals(1, monolithRequests.size());
		assertEquals("/v3/api-docs", monolithRequests.getFirst().path());
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
			// Simulate downstream CORS headers so gateway RemoveResponseHeader is exercised.
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
