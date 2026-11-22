package com.swissotel.swissotel360.billing.application;

import com.swissotel.swissotel360.billing.application.command.*;
import com.swissotel.swissotel360.billing.application.internal.commandservices.BillingCommandServiceImpl;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.BillingCollaboratorsOutboundService;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.ServiceRequestCollaboratorSnapshot;
import com.swissotel.swissotel360.billing.application.port.ProcessedEventStore;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.exceptions.ChargeServiceRequestNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioAlreadyPaidException;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import com.swissotel.swissotel360.billing.domain.model.PaymentId;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import com.swissotel.swissotel360.billing.domain.repositories.FolioRepository;
import com.swissotel.swissotel360.billing.domain.repositories.InvoiceRepository;
import com.swissotel.swissotel360.billing.domain.repositories.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BillingCommandServiceImplTest {

	private static final Instant NOW = Instant.parse("2026-11-22T18:00:00Z");
	private final FolioRepository folios = mock(FolioRepository.class);
	private final PaymentRepository payments = mock(PaymentRepository.class);
	private final InvoiceRepository invoices = mock(InvoiceRepository.class);
	private final BillingCollaboratorsOutboundService collaborators = mock(BillingCollaboratorsOutboundService.class);
	private final ProcessedEventStore processedEvents = mock(ProcessedEventStore.class);
	private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

	private final BillingCommandServiceImpl service = new BillingCommandServiceImpl(
			folios, payments, invoices, collaborators, processedEvents, clock);

	@Test
	void registerFolioCreatesOnceAndSkipsWhenAlreadyProcessedOrPresent() {
		when(processedEvents.exists("stay.checked-in:42")).thenReturn(false);
		when(folios.findByStayId(42L)).thenReturn(Optional.empty());
		when(folios.save(any(Folio.class))).thenAnswer(inv -> inv.getArgument(0));

		service.registerFolio(new RegisterFolioCommand(42L));

		verify(folios).save(any(Folio.class));
		verify(processedEvents).markProcessed("stay.checked-in:42", "stay.checked-in", NOW);

		when(processedEvents.exists("stay.checked-in:42")).thenReturn(true);
		service.registerFolio(new RegisterFolioCommand(42L));
		verify(folios).save(any(Folio.class));
	}

	@Test
	void recordServiceRequestChargeIsIdempotentAndSkipsMissingOpenFolio() {
		var openFolio = Folio.restore(FolioId.newId(), 10L, 42L, FolioStatus.OPEN, List.of());
		when(processedEvents.exists("service-request.completed:99")).thenReturn(false);
		when(folios.existsChargeForServiceRequest(99L)).thenReturn(false);
		when(folios.findByStayId(42L)).thenReturn(Optional.of(openFolio));
		when(folios.save(any(Folio.class))).thenAnswer(inv -> inv.getArgument(0));

		service.recordServiceRequestCharge(new RecordServiceRequestChargeCommand(
				42L, 99L, "Room Service", new BigDecimal("55.00")));

		assertEquals(1, openFolio.charges().size());
		verify(folios).save(openFolio);
		verify(processedEvents).markProcessed("service-request.completed:99", "service-request.completed", NOW);

		when(processedEvents.exists("service-request.completed:100")).thenReturn(false);
		when(folios.existsChargeForServiceRequest(100L)).thenReturn(false);
		when(folios.findByStayId(999L)).thenReturn(Optional.empty());
		service.recordServiceRequestCharge(new RecordServiceRequestChargeCommand(
				999L, 100L, "Spa", new BigDecimal("80.00")));
		verify(processedEvents, never()).markProcessed("service-request.completed:100", "service-request.completed", NOW);
	}

	@Test
	void addChargeResolvesServiceRequestAndRejectsMismatchedStay() {
		var folioId = UUID.randomUUID();
		var reqPublicId = UUID.randomUUID();
		var folio = Folio.restore(new FolioId(folioId), 10L, 42L, FolioStatus.OPEN, List.of());

		when(folios.findByPublicId(new FolioId(folioId))).thenReturn(Optional.of(folio));
		when(collaborators.findServiceRequest(reqPublicId)).thenReturn(Optional.of(
				new ServiceRequestCollaboratorSnapshot(77L, reqPublicId, 42L, "spa", "completed")));
		when(folios.save(any(Folio.class))).thenAnswer(inv -> inv.getArgument(0));

		var charge = service.addCharge(new AddChargeCommand(folioId, "Spa", new BigDecimal("120.00"), reqPublicId));
		assertEquals(77L, charge.serviceRequestId());

		var wrongStayReq = UUID.randomUUID();
		when(collaborators.findServiceRequest(wrongStayReq)).thenReturn(Optional.of(
				new ServiceRequestCollaboratorSnapshot(78L, wrongStayReq, 999L, "spa", "completed")));
		assertThrows(ChargeServiceRequestNotFoundException.class,
				() -> service.addCharge(new AddChargeCommand(folioId, "Spa", new BigDecimal("120.00"), wrongStayReq)));
	}

	@Test
	void processPaymentAndIssueInvoicePersistExpectedAggregates() {
		var folioId = UUID.randomUUID();
		var paymentId = UUID.randomUUID();
		var folio = Folio.restore(new FolioId(folioId), 10L, 42L, FolioStatus.OPEN,
				List.of(new Charge(1L, UUID.randomUUID(), null, "Minibar", new BigDecimal("40.00"), NOW)));
		var method = new PaymentMethod(1L, "cash", "Efectivo");

		when(folios.findByPublicId(new FolioId(folioId))).thenReturn(Optional.of(folio));
		when(payments.findMethodByCode("cash")).thenReturn(Optional.of(method));
		when(payments.existsByFolioId(10L)).thenReturn(false);
		when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
		when(folios.save(any(Folio.class))).thenAnswer(inv -> inv.getArgument(0));

		var payment = service.processPayment(new ProcessPaymentCommand(folioId, "cash", new BigDecimal("40.00")));
		assertEquals(new BigDecimal("40.00"), payment.amount());
		assertEquals(FolioStatus.CLOSED, folio.status());

		when(payments.existsByFolioId(10L)).thenReturn(true);
		assertThrows(FolioAlreadyPaidException.class,
				() -> service.processPayment(new ProcessPaymentCommand(folioId, "cash", new BigDecimal("40.00"))));

		var persistedPayment = Payment.restore(new PaymentId(paymentId), 20L, 10L, method, new BigDecimal("40.00"),
				com.swissotel.swissotel360.billing.domain.model.PaymentStatus.PROCESSED, "SIM-1234", NOW);
		when(payments.findByPublicId(new PaymentId(paymentId))).thenReturn(Optional.of(persistedPayment));
		when(invoices.existsByPaymentId(20L)).thenReturn(false);
		when(invoices.findLastNumber("B001")).thenReturn(Optional.of("00000004"));
		when(invoices.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

		var invoice = service.issueInvoice(new IssueInvoiceCommand(paymentId, DocumentType.RECEIPT));
		assertEquals("B001", invoice.series());
		assertEquals("00000005", invoice.number());
	}
}
