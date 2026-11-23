package com.swissotel.swissotel360.operations;

import com.swissotel.swissotel360.servicerequests.application.command.CreateServiceRequestCommand;
import com.swissotel.swissotel360.servicerequests.application.commandservices.ServiceRequestCommandService;
import com.swissotel.swissotel360.servicerequests.application.queryservices.ServiceRequestQueryService;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.ServiceRequestController;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.ServiceRequestsExceptionHandler;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.ServiceTypeController;
import com.swissotel.swissotel360.shared.infrastructure.autoconfigure.SharedKernelAutoConfiguration;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessSecurityAutoConfiguration;
import com.swissotel.swissotel360.stays.application.command.CheckInCommand;
import com.swissotel.swissotel360.stays.application.commandservices.StayCommandService;
import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.application.queryservices.StayQueryService;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;
import com.swissotel.swissotel360.stays.domain.model.KeyStatus;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.interfaces.rest.DigitalKeyController;
import com.swissotel.swissotel360.stays.interfaces.rest.StayController;
import com.swissotel.swissotel360.stays.interfaces.rest.StaysExceptionHandler;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { StayController.class, DigitalKeyController.class, ServiceRequestController.class,
		ServiceTypeController.class })
@ImportAutoConfiguration({
		SecurityAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		StatelessSecurityAutoConfiguration.class,
		SharedKernelAutoConfiguration.class
})
@Import({ StaysExceptionHandler.class, ServiceRequestsExceptionHandler.class })
@TestPropertySource(properties = "authorization.jwt.secret=swissotel-360-unit-test-jwt-secret-key-min-32-bytes")
class OperationsRestSecurityWebTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StayCommandService stayCommandService;

	@MockitoBean
	private StayQueryService stayQueryService;

	@MockitoBean
	private ServiceRequestCommandService serviceRequestCommandService;

	@MockitoBean
	private ServiceRequestQueryService serviceRequestQueryService;

	@Test
	void enforcesAuthAndRbacAndNeverExposesInternalIds() throws Exception {
		mockMvc.perform(get("/api/v1/stays"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		var noPermsToken = createJwt("ROLE_GUEST", List.of("guests.view"));
		mockMvc.perform(get("/api/v1/stays").header(HttpHeaders.AUTHORIZATION, "Bearer " + noPermsToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));

		var stayUuid = UUID.randomUUID();
		var roomUuid = UUID.randomUUID();
		var now = Instant.parse("2026-11-22T14:00:00Z");
		var stay = Stay.restore(
				new StayId(stayUuid),
				12L,
				100L,
				200L,
				10L,
				now,
				null,
				StayStatus.ACTIVE,
				2L,
				List.of(DigitalKey.restore(44L, UUID.randomUUID(), "K7M2Q9XA4TBD", now, null, KeyStatus.ACTIVE)));
		var stayView = new StayView(stay, roomUuid, "301");
		var serviceType = new ServiceType(1L, "room_service", "Room Service", "F&B");
		var serviceRequest = ServiceRequest.register(12L, serviceType, "Two coffees to room 301", now);

		when(stayQueryService.list(null)).thenReturn(List.of(stayView));
		when(stayCommandService.checkIn(any(CheckInCommand.class))).thenReturn(stayView);
		when(serviceRequestQueryService.listServiceTypes()).thenReturn(List.of(serviceType));
		when(serviceRequestCommandService.create(any(CreateServiceRequestCommand.class))).thenReturn(serviceRequest);

		var operatorToken = createJwt("ROLE_RECEPTIONIST",
				List.of("stays.checkin", "stays.checkout", "service_requests.view", "service_requests.create"));

		mockMvc.perform(get("/api/v1/stays").header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].stayId").value(stayUuid.toString()))
				.andExpect(jsonPath("$[0].roomId").value(roomUuid.toString()))
				.andExpect(jsonPath("$[0].roomNumber").value("301"))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/stays/check-in")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reservationCode":"SW26-7H3KQ9XA","documentType":"DNI","documentNumber":"12345678"}"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.stayId").value(stayUuid.toString()))
				.andExpect(jsonPath("$.accessCode").value("K7M2Q9XA4TBD"))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(get("/api/v1/service-types").header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("room_service"))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/service-requests")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"stayId":"%s","serviceTypeCode":"room_service","description":"Two coffees to room 301"}"""
								.formatted(stayUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.requestId").value(serviceRequest.id().value().toString()))
				.andExpect(jsonPath("$.status").value("registered"))
				.andExpect(jsonPath("$..id").isEmpty());
	}

	private static String createJwt(String role, List<String> permissions) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject("ops@swissotel.com")
				.claim("uid", 2L)
				.claim("pid", UUID.randomUUID().toString())
				.claim("role", role)
				.claim("perms", permissions)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(key)
				.compact();
	}
}
