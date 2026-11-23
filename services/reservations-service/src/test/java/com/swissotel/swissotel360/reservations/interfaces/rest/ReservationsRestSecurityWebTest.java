package com.swissotel.swissotel360.reservations.interfaces.rest;

import com.swissotel.swissotel360.reservations.application.command.CreateReservationCommand;
import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.dto.ReservationResult;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort.CurrentActor;
import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort.GuestSnapshot;
import com.swissotel.swissotel360.reservations.application.port.PrepaymentPort;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.NightPrice;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.PriceQuote;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.RoomTypeSnapshot;
import com.swissotel.swissotel360.reservations.application.query.GetReservationQuery;
import com.swissotel.swissotel360.reservations.application.queryservices.ReservationQueryService;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.model.GuestId;
import com.swissotel.swissotel360.reservations.domain.model.PaymentType;
import com.swissotel.swissotel360.reservations.domain.model.ReservationCode;
import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { ReservationController.class })
@ImportAutoConfiguration({
		SecurityAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		StatelessSecurityAutoConfiguration.class,
		SharedKernelAutoConfiguration.class
})
@Import(ReservationsExceptionHandler.class)
@TestPropertySource(properties = "authorization.jwt.secret=swissotel-360-unit-test-jwt-secret-key-min-32-bytes")
class ReservationsRestSecurityWebTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReservationCommandService commandService;

	@MockitoBean
	private ReservationQueryService queryService;

	@MockitoBean
	private CurrentActorPort currentActorPort;

	@Test
	void enforcesAuthAndRbacAndNeverExposesInternalIds() throws Exception {
		var reservationUuid = UUID.randomUUID();
		var guestUuid = UUID.randomUUID();
		var roomTypeUuid = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/reservations/{publicId}", reservationUuid))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		var limitedToken = createJwt("ROLE_HOUSEKEEPING", List.of("service_requests.view"));
		mockMvc.perform(get("/api/v1/reservations/{publicId}", reservationUuid)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + limitedToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));

		var checkIn = LocalDate.now().plusDays(7);
		var checkOut = checkIn.plusDays(2);
		var actor = new CurrentActor(new UserId(2L), "ROLE_RECEPTIONIST",
				Set.of("reservations.view", "reservations.create"));
		var reservation = Reservation.restore(
				new ReservationId(reservationUuid),
				new ReservationCode("SW26-7H3KQ9XA"),
				new GuestId(10L),
				new RoomTypeId(3L),
				new StayPeriod(checkIn, checkOut),
				ReservationStatus.PENDING,
				PaymentType.DEFERRED,
				new Money(new BigDecimal("760.00")),
				new UserId(2L),
				Instant.parse("2026-11-22T12:00:00Z"),
				null,
				null,
				null);
		var result = new ReservationResult(
				reservation,
				new GuestSnapshot(new GuestId(10L), guestUuid, "Ana Torres", "ana@swissotel.com"),
				new RoomTypeSnapshot(new RoomTypeId(3L), roomTypeUuid, "Deluxe", (short) 2),
				new PriceQuote(
						new Money(new BigDecimal("760.00")),
						List.of(
								new NightPrice(checkIn, new Money(new BigDecimal("380.00"))),
								new NightPrice(checkIn.plusDays(1), new Money(new BigDecimal("380.00"))))),
				Instant.parse("2026-11-22T12:30:00Z"),
				PrepaymentPort.PrepaymentRequest.unavailable());

		when(currentActorPort.current()).thenReturn(actor);
		when(queryService.findByPublicId(eq(new GetReservationQuery(reservationUuid)), eq(actor)))
				.thenReturn(result);
		when(commandService.create(any(CreateReservationCommand.class))).thenReturn(result);

		var receptionistToken = createJwt("ROLE_RECEPTIONIST",
				List.of("reservations.view", "reservations.create"));

		mockMvc.perform(get("/api/v1/reservations/{publicId}", reservationUuid)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + receptionistToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.publicId").value(reservationUuid.toString()))
				.andExpect(jsonPath("$.reservationCode").value("SW26-7H3KQ9XA"))
				.andExpect(jsonPath("$.guest.publicId").value(guestUuid.toString()))
				.andExpect(jsonPath("$.roomType.publicId").value(roomTypeUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/reservations")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + receptionistToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"guestPublicId":"%s","roomTypePublicId":"%s","checkInDate":"%s","checkOutDate":"%s","paymentType":"deferred"}"""
								.formatted(guestUuid, roomTypeUuid, checkIn, checkOut)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.publicId").value(reservationUuid.toString()))
				.andExpect(jsonPath("$.guest.publicId").value(guestUuid.toString()))
				.andExpect(jsonPath("$.roomType.publicId").value(roomTypeUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());
	}

	private static String createJwt(String role, List<String> permissions) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject("reception@swissotel.com")
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
