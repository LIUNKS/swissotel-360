package com.swissotel.swissotel360.shared.interfaces.rest;

import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void methodArgumentTypeMismatchAndUnreadableBodyNeverLeakInternalClassNames() throws NoSuchMethodException {
		var method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleEndpoint", UUID.class);
		var parameter = new MethodParameter(method, 0);
		var typeMismatch = new MethodArgumentTypeMismatchException(
				"not-a-uuid",
				UUID.class,
				"stayId",
				parameter,
				new IllegalArgumentException("Failed to convert com.swissotel.swissotel360.stays.StayId from java.lang.String"));

		var mismatchResponse = handler.handleMethodArgumentTypeMismatch(typeMismatch);
		assertEquals(HttpStatus.BAD_REQUEST, mismatchResponse.getStatusCode());
		assertNotNull(mismatchResponse.getBody());
		assertEquals("INVALID_REQUEST", mismatchResponse.getBody().code());
		assertEquals("Invalid request parameter", mismatchResponse.getBody().message());
		assertEquals("Invalid value for parameter 'stayId'", mismatchResponse.getBody().details());
		assertNoInternalLeak(mismatchResponse.getBody());

		var nullNameMismatch = new MethodArgumentTypeMismatchException("x", UUID.class, null, parameter, null);
		var nullNameResponse = handler.handleMethodArgumentTypeMismatch(nullNameMismatch);
		assertEquals(HttpStatus.BAD_REQUEST, nullNameResponse.getStatusCode());
		assertNotNull(nullNameResponse.getBody());
		assertEquals("Invalid value for parameter 'unknown'", nullNameResponse.getBody().details());

		var notReadable = new HttpMessageNotReadableException(
				"JSON parse error: Cannot deserialize value of type com.swissotel.swissotel360.billing.domain.model.DocumentType from org.springframework.http.Input",
				new RuntimeException("java.lang.IllegalArgumentException: com.swissotel.internal"),
				new MockHttpInputMessage(new byte[0]));

		var notReadableResponse = handler.handleHttpMessageNotReadable(notReadable);
		assertEquals(HttpStatus.BAD_REQUEST, notReadableResponse.getStatusCode());
		assertNotNull(notReadableResponse.getBody());
		assertEquals("INVALID_REQUEST", notReadableResponse.getBody().code());
		assertEquals("Invalid request body", notReadableResponse.getBody().message());
		assertEquals("Malformed JSON payload or invalid field value", notReadableResponse.getBody().details());
		assertNoInternalLeak(notReadableResponse.getBody());
	}

	@Test
	void runtimeAndGenericExceptionsMaskSqlAndJavaInternals() {
		var runtimeResponse = handler.handleRuntimeException(new RuntimeException(
				"JDBC exception executing SQL [SELECT * FROM reservations]: org.hibernate.exception.SQLGrammarException in com.swissotel.swissotel360.Foo"));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, runtimeResponse.getStatusCode());
		assertNotNull(runtimeResponse.getBody());
		assertEquals("UNEXPECTED_ERROR", runtimeResponse.getBody().code());
		assertNotNull(runtimeResponse.getBody().message());
		assertFalse(runtimeResponse.getBody().message().isBlank());
		assertEquals("An unexpected internal error occurred", runtimeResponse.getBody().details());
		assertNoInternalLeak(runtimeResponse.getBody());
		assertFalse(runtimeResponse.getBody().details().contains("SQL"));
		assertFalse(runtimeResponse.getBody().details().contains("Hibernate"));

		var checkedResponse = handler.handleException(new Exception(
				"java.sql.SQLException: relation com.swissotel.swissotel360.secret_table does not exist (org.postgresql.util.PSQLException)"));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, checkedResponse.getStatusCode());
		assertNotNull(checkedResponse.getBody());
		assertEquals("UNEXPECTED_ERROR", checkedResponse.getBody().code());
		assertNotNull(checkedResponse.getBody().message());
		assertFalse(checkedResponse.getBody().message().isBlank());
		assertEquals("An unexpected internal error occurred", checkedResponse.getBody().details());
		assertNoInternalLeak(checkedResponse.getBody());
		assertFalse(checkedResponse.getBody().details().contains("secret_table"));
	}

	@Test
	void validationAndIllegalArgumentHandlersReturnValidationErrorResource() throws NoSuchMethodException {
		var method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleEndpoint", UUID.class);
		var parameter = new MethodParameter(method, 0);
		var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
		bindingResult.addError(new FieldError("request", "documentNumber", "must not be blank"));
		var notValid = new MethodArgumentNotValidException(parameter, bindingResult);

		var validationResponse = handler.handleMethodArgumentNotValid(notValid);
		assertEquals(HttpStatus.BAD_REQUEST, validationResponse.getStatusCode());
		assertNotNull(validationResponse.getBody());
		assertEquals("VALIDATION_ERROR", validationResponse.getBody().code());
		assertTrue(validationResponse.getBody().details().contains("documentNumber"));
		assertNoInternalLeak(validationResponse.getBody());

		var illegalArgResponse = handler.handleIllegalArgumentException(
				new IllegalArgumentException("Invalid stay status: zzz"));
		assertEquals(HttpStatus.BAD_REQUEST, illegalArgResponse.getStatusCode());
		assertNotNull(illegalArgResponse.getBody());
		assertEquals("VALIDATION_ERROR", illegalArgResponse.getBody().code());
		assertEquals("Invalid stay status: zzz", illegalArgResponse.getBody().details());
		assertNoInternalLeak(illegalArgResponse.getBody());
	}

	private static void assertNoInternalLeak(ErrorResource resource) {
		for (var value : List.of(resource.code(), resource.message(),
				resource.details() == null ? "" : resource.details())) {
			assertFalse(value.contains("com.swissotel"), "Leaked package com.swissotel in: " + value);
			assertFalse(value.contains("java."), "Leaked package java. in: " + value);
			assertFalse(value.contains("org."), "Leaked package org. in: " + value);
		}
	}

	@SuppressWarnings("unused")
	private void sampleEndpoint(UUID stayId) {
	}
}
