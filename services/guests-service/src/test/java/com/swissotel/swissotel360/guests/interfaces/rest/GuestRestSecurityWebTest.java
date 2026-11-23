package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.application.commandservices.GuestCommandService;
import com.swissotel.swissotel360.guests.application.queryservices.GuestQueryService;
import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.commands.CreateGuestCommand;
import com.swissotel.swissotel360.guests.domain.model.valueobjects.DocumentType;
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

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { GuestController.class, DocumentTypeController.class })
@ImportAutoConfiguration({
		SecurityAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		StatelessSecurityAutoConfiguration.class,
		SharedKernelAutoConfiguration.class
})
@Import(GuestsExceptionHandler.class)
@TestPropertySource(properties = "authorization.jwt.secret=swissotel-360-unit-test-jwt-secret-key-min-32-bytes")
class GuestRestSecurityWebTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GuestCommandService commandService;

	@MockitoBean
	private GuestQueryService queryService;

	@Test
	void enforcesAuthAndRbacAndNeverExposesInternalIds() throws Exception {
		mockMvc.perform(get("/api/v1/document-types"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		var guestToken = createJwt("ROLE_GUEST", List.of("reservations.view"));
		mockMvc.perform(get("/api/v1/guests").header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));

		var guestPublicId = UUID.randomUUID();
		var guest = new Guest(10L, guestPublicId, null, 1L, "DNI", "12345678",
				"Ana", "Garcia", "ana@swissotel.com", "+51999111222", "Peru", OffsetDateTime.now());
		when(queryService.getDocumentTypes()).thenReturn(List.of(new DocumentType(1L, "DNI", "DNI", true)));
		when(commandService.create(any(CreateGuestCommand.class))).thenReturn(guest);

		var staffToken = createJwt("ROLE_RECEPTIONIST", List.of("guests.view", "guests.manage"));
		mockMvc.perform(get("/api/v1/document-types").header(HttpHeaders.AUTHORIZATION, "Bearer " + staffToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("DNI"))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/guests")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + staffToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"documentTypeCode":"DNI","documentNumber":"12345678","firstName":"Ana","lastName":"Garcia","email":"ana@swissotel.com"}"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.publicId").value(guestPublicId.toString()))
				.andExpect(jsonPath("$..id").isEmpty());
	}

	private static String createJwt(String role, List<String> permissions) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject("staff@swissotel.com")
				.claim("uid", 9L)
				.claim("pid", UUID.randomUUID().toString())
				.claim("role", role)
				.claim("perms", permissions)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(key)
				.compact();
	}
}
