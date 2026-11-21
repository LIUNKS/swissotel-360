package com.swissotel.swissotel360;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none",
		"reservations.expiration-job.enabled=false" })
class Swissotel360ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void protectedBoundedContextEndpointsRequireAuthentication(@LocalServerPort int port) throws Exception {
		var publicId = UUID.randomUUID();
		var paths = List.of(
				"/api/v1/rooms",
				"/api/v1/room-types",
				"/api/v1/room-types/" + publicId,
				"/api/v1/room-types/" + publicId + "/rates",
				"/api/v1/availability?checkIn=2026-11-01&checkOut=2026-11-03",
				"/api/v1/room-types/" + publicId + "/quote?checkIn=2026-11-01&checkOut=2026-11-03",
				"/api/v1/reservations",
				"/api/v1/reservations/" + publicId,
				"/api/v1/reservations/by-code/SW26-7H3KQ9XA");

		try (var client = HttpClient.newHttpClient()) {
			for (var path : paths) {
				var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
				var response = client.send(request, HttpResponse.BodyHandlers.discarding());
				assertEquals(401, response.statusCode(), "Unexpected response for " + path);
			}
		}
	}
}
