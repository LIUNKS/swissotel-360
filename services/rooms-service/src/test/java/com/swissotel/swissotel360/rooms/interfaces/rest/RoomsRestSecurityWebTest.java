package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.application.commandservices.RateCommandService;
import com.swissotel.swissotel360.rooms.application.commandservices.RoomCommandService;
import com.swissotel.swissotel360.rooms.application.commandservices.RoomTypeCommandService;
import com.swissotel.swissotel360.rooms.application.queryservices.AvailabilityQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RateQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomTypeQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomTypeView;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomView;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomCommand;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.interfaces.rest.security.RoomStatusAuthorizationService;
import com.swissotel.swissotel360.shared.infrastructure.autoconfigure.SharedKernelAutoConfiguration;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessSecurityAutoConfiguration;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { RoomController.class, RoomTypeController.class, RateController.class,
		AvailabilityController.class })
@ImportAutoConfiguration({
		SecurityAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		StatelessSecurityAutoConfiguration.class,
		SharedKernelAutoConfiguration.class
})
@Import({ RoomsExceptionHandler.class, RoomStatusAuthorizationService.class })
@TestPropertySource(properties = "authorization.jwt.secret=swissotel-360-unit-test-jwt-secret-key-min-32-bytes")
class RoomsRestSecurityWebTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RoomCommandService roomCommandService;

	@MockitoBean
	private RoomQueryService roomQueryService;

	@MockitoBean
	private RoomTypeCommandService roomTypeCommandService;

	@MockitoBean
	private RoomTypeQueryService roomTypeQueryService;

	@MockitoBean
	private RateCommandService rateCommandService;

	@MockitoBean
	private RateQueryService rateQueryService;

	@MockitoBean
	private AvailabilityQueryService availabilityQueryService;

	@Test
	void enforcesAuthAndRbacAndNeverExposesInternalIds() throws Exception {
		mockMvc.perform(get("/api/v1/rooms"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		var guestToken = createJwt("ROLE_GUEST", List.of("reservations.view"));
		mockMvc.perform(get("/api/v1/rooms").header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));

		var roomTypeUuid = UUID.randomUUID();
		var roomUuid = UUID.randomUUID();
		var roomType = new RoomType(2L, roomTypeUuid, "Deluxe", (short) 3, "Luxury");
		var roomTypeView = new RoomTypeView(roomType, new BigDecimal("380.00"));
		var room = new Room(5L, roomUuid, 2L, "301", (short) 3, RoomStatus.AVAILABLE);
		var view = new RoomView(room, roomTypeUuid, "Deluxe");

		when(roomTypeQueryService.findAll()).thenReturn(List.of(roomTypeView));
		when(roomCommandService.create(any(CreateRoomCommand.class))).thenReturn(room);
		when(roomQueryService.findByPublicId(new GetRoomByPublicIdQuery(roomUuid))).thenReturn(view);

		var managerToken = createJwt("ROLE_MANAGER", List.of("rooms.view", "rooms.manage"));
		mockMvc.perform(get("/api/v1/room-types").header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].publicId").value(roomTypeUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/rooms")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"roomTypePublicId":"%s","roomNumber":"301","floor":3}"""
								.formatted(roomTypeUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.publicId").value(roomUuid.toString()))
				.andExpect(jsonPath("$.roomTypePublicId").value(roomTypeUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());
	}

	private static String createJwt(String role, List<String> permissions) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject("manager@swissotel.com")
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
