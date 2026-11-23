package com.swissotel.swissotel360.shared.infrastructure.security;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UnauthorizedRequestHandlerEntryPointTest {

	private final UnauthorizedRequestHandlerEntryPoint entryPoint = new UnauthorizedRequestHandlerEntryPoint();

	@Test
	void commenceWrites401JsonErrorResourceWithoutExposingExceptionMessage() throws Exception {
		var request = new MockHttpServletRequest("GET", "/api/v1/users/1");
		var response = new MockHttpServletResponse();
		var exception = new AuthenticationException("Internal JWT signature failure") {
		};

		entryPoint.commence(request, response, exception);

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

		String body = response.getContentAsString();
		var document = JsonPath.parse(body);
		assertEquals("UNAUTHORIZED", document.read("$.code", String.class));
		assertEquals("Authentication required", document.read("$.message", String.class));
		assertFalse(body.contains("details"));
		assertFalse(body.contains("Internal JWT signature failure"));
	}
}
