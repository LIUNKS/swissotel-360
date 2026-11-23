package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IamErrorResponseAssemblerTest {

	private Locale previousDefaultLocale;

	@BeforeEach
	void setUp() {
		previousDefaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
	}

	@AfterEach
	void tearDown() {
		LocaleContextHolder.resetLocaleContext();
		Locale.setDefault(previousDefaultLocale);
	}

	@Test
	void mapsUnauthorizedErrorCodeTo401AndLocalizedMessage() {
		assertEquals(HttpStatus.UNAUTHORIZED, ErrorResponseAssembler.toStatusFromErrorCode("UNAUTHORIZED"));

		var error = ApplicationError.unauthorized("Invalid email or password");

		LocaleContextHolder.setLocale(Locale.ENGLISH);
		var responseEn = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
		assertEquals(HttpStatus.UNAUTHORIZED, responseEn.getStatusCode());
		assertNotNull(responseEn.getBody());
		assertEquals("UNAUTHORIZED", responseEn.getBody().code());
		assertEquals("Invalid credentials", responseEn.getBody().message());
		assertEquals("Invalid email or password", responseEn.getBody().details());

		LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
		var responseEs = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
		assertEquals(HttpStatus.UNAUTHORIZED, responseEs.getStatusCode());
		assertNotNull(responseEs.getBody());
		assertEquals("UNAUTHORIZED", responseEs.getBody().code());
		assertEquals("Credenciales invalidas", responseEs.getBody().message());
		assertEquals("Invalid email or password", responseEs.getBody().details());
	}

	@Test
	void mapsUserNotFoundErrorCodeTo404AndLocalizedMessage() {
		var error = ApplicationError.notFound("user", "42");

		LocaleContextHolder.setLocale(Locale.ENGLISH);
		var responseEn = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
		assertEquals(HttpStatus.NOT_FOUND, responseEn.getStatusCode());
		assertNotNull(responseEn.getBody());
		assertEquals("USER_NOT_FOUND", responseEn.getBody().code());
		assertEquals("User not found", responseEn.getBody().message());
		assertNull(responseEn.getBody().details());

		LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
		var responseEs = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
		assertEquals(HttpStatus.NOT_FOUND, responseEs.getStatusCode());
		assertNotNull(responseEs.getBody());
		assertEquals("USER_NOT_FOUND", responseEs.getBody().code());
		assertEquals("Usuario no encontrado", responseEs.getBody().message());
		assertNull(responseEs.getBody().details());
	}
}
