package com.swissotel.swissotel360.billing.interfaces.events;

import com.swissotel.swissotel360.billing.application.command.RecordServiceRequestChargeCommand;
import com.swissotel.swissotel360.billing.application.command.RegisterFolioCommand;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.application.port.ProcessedEventStore;
import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BillingIntegrationEventListenerTest {

	private final BillingCommandService commands = mock(BillingCommandService.class);
	private final BillingQueryService queries = mock(BillingQueryService.class);
	private final ProcessedEventStore processedEvents = mock(ProcessedEventStore.class);
	private final BillingIntegrationEventListener listener = new BillingIntegrationEventListener(
			commands, queries, processedEvents);

	@Test
	void handlesStayCheckedInAndRejectsMalformedPayloadsWithoutRequeue() {
		var event = new StayCheckedInEventMessage(UUID.randomUUID(), 42L, 1L, 2L, 3L, Instant.now());
		listener.onStayCheckedIn(event);
		verify(commands).registerFolio(new RegisterFolioCommand(42L));

		assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onStayCheckedIn(null));
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> listener.onStayCheckedIn(new StayCheckedInEventMessage(UUID.randomUUID(), null, 1L, 2L, 3L, null)));
	}

	@Test
	void handlesConcurrentStayCheckedInDuplicateIdempotentlyWhileRethrowingRealIntegrityErrors() {
		var event = new StayCheckedInEventMessage(UUID.randomUUID(), 42L, 1L, 2L, 3L, Instant.now());
		doThrow(new DataIntegrityViolationException("duplicate")).when(commands)
				.registerFolio(new RegisterFolioCommand(42L));

		when(queries.findFolioByStayId(42L)).thenReturn(Optional.of(Folio.open(42L)));
		assertDoesNotThrow(() -> listener.onStayCheckedIn(event));

		when(queries.findFolioByStayId(42L)).thenReturn(Optional.empty());
		when(processedEvents.exists("stay.checked-in:42")).thenReturn(false);
		assertThrows(DataIntegrityViolationException.class, () -> listener.onStayCheckedIn(event));
	}

	@Test
	void handlesServiceRequestCompletedSkipsNonBillableAndRejectsInvalidMessages() {
		var billable = new ServiceRequestCompletedEventMessage(
				UUID.randomUUID(), 77L, 42L, "spa", "Spa", new BigDecimal("95.00"), Instant.now());
		listener.onServiceRequestCompleted(billable);
		verify(commands).recordServiceRequestCharge(
				new RecordServiceRequestChargeCommand(42L, 77L, "Spa", new BigDecimal("95.00")));

		var zero = new ServiceRequestCompletedEventMessage(
				UUID.randomUUID(), 78L, 42L, "housekeeping", "Limpieza", BigDecimal.ZERO, Instant.now());
		listener.onServiceRequestCompleted(zero);

		var nonBillable = new ServiceRequestCompletedEventMessage(
				UUID.randomUUID(), 79L, 42L, "housekeeping", "Limpieza", null, Instant.now());
		listener.onServiceRequestCompleted(nonBillable);

		verifyNoInteractions(processedEvents);

		assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onServiceRequestCompleted(null));
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> listener.onServiceRequestCompleted(new ServiceRequestCompletedEventMessage(
						UUID.randomUUID(), null, 42L, "spa", "Spa", BigDecimal.TEN, Instant.now())));
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> listener.onServiceRequestCompleted(new ServiceRequestCompletedEventMessage(
						UUID.randomUUID(), 80L, 42L, " ", " ", BigDecimal.TEN, Instant.now())));
	}
}
