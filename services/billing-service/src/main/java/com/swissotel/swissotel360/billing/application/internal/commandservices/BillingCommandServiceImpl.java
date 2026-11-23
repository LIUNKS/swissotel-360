package com.swissotel.swissotel360.billing.application.internal.commandservices;

import com.swissotel.swissotel360.billing.application.command.*;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.BillingCollaboratorsOutboundService;
import com.swissotel.swissotel360.billing.application.port.ProcessedEventStore;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.exceptions.*;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.model.PaymentId;
import com.swissotel.swissotel360.billing.domain.repositories.FolioRepository;
import com.swissotel.swissotel360.billing.domain.repositories.InvoiceRepository;
import com.swissotel.swissotel360.billing.domain.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public final class BillingCommandServiceImpl implements BillingCommandService {
	private static final Logger log = LoggerFactory.getLogger(BillingCommandServiceImpl.class);
	private static final String STAY_CHECKED_IN_EVENT = "stay.checked-in";
	private static final String SERVICE_REQUEST_COMPLETED_EVENT = "service-request.completed";

	private final FolioRepository folios;
	private final PaymentRepository payments;
	private final InvoiceRepository invoices;
	private final BillingCollaboratorsOutboundService collaborators;
	private final ProcessedEventStore processedEvents;
	private final Clock clock;

	public BillingCommandServiceImpl(FolioRepository folios, PaymentRepository payments, InvoiceRepository invoices,
			BillingCollaboratorsOutboundService collaborators, ProcessedEventStore processedEvents, Clock clock) {
		this.folios = folios;
		this.payments = payments;
		this.invoices = invoices;
		this.collaborators = collaborators;
		this.processedEvents = processedEvents;
		this.clock = clock;
	}

	@Override
	public void registerFolio(RegisterFolioCommand command) {
		var eventKey = STAY_CHECKED_IN_EVENT + ":" + command.stayId();
		if (processedEvents.exists(eventKey)) {
			log.debug("Integration event {} already processed for stay {}", eventKey, command.stayId());
			return;
		}
		if (folios.findByStayId(command.stayId()).isPresent()) {
			log.debug("Folio already exists for stay {}", command.stayId());
		} else {
			folios.save(Folio.open(command.stayId()));
		}
		processedEvents.markProcessed(eventKey, STAY_CHECKED_IN_EVENT, Instant.now(clock));
	}

	@Override
	public Charge addCharge(AddChargeCommand command) {
		var folio = folios.findByPublicId(new FolioId(command.folioId()))
				.orElseThrow(() -> new FolioNotFoundException(command.folioId()));
		Long serviceRequestId = null;
		if (command.serviceRequestId() != null) {
			var request = collaborators.findServiceRequest(command.serviceRequestId())
					.filter(r -> r.stayId().equals(folio.stayId()))
					.orElseThrow(() -> new ChargeServiceRequestNotFoundException(command.serviceRequestId()));
			serviceRequestId = request.id();
		}
		folio.addCharge(command.concept(), command.amount(), serviceRequestId, Instant.now(clock));
		var charges = folios.save(folio).charges();
		return charges.get(charges.size() - 1);
	}

	@Override
	public void recordServiceRequestCharge(RecordServiceRequestChargeCommand command) {
		var eventKey = SERVICE_REQUEST_COMPLETED_EVENT + ":" + command.serviceRequestId();
		if (processedEvents.exists(eventKey)) {
			log.debug("Integration event {} already processed for service request {}", eventKey,
					command.serviceRequestId());
			return;
		}
		if (folios.existsChargeForServiceRequest(command.serviceRequestId())) {
			log.debug("Charge already recorded for service request {}", command.serviceRequestId());
			processedEvents.markProcessed(eventKey, SERVICE_REQUEST_COMPLETED_EVENT, Instant.now(clock));
			return;
		}
		var folio = folios.findByStayId(command.stayId()).filter(Folio::isOpen).orElse(null);
		if (folio == null) {
			log.warn("No open folio for stay {}; skipping charge of service request {}", command.stayId(),
					command.serviceRequestId());
			return;
		}
		folio.addCharge(command.concept(), command.amount(), command.serviceRequestId(), Instant.now(clock));
		folios.save(folio);
		processedEvents.markProcessed(eventKey, SERVICE_REQUEST_COMPLETED_EVENT, Instant.now(clock));
	}

	@Override
	public Payment processPayment(ProcessPaymentCommand command) {
		var folio = folios.findByPublicId(new FolioId(command.folioId()))
				.orElseThrow(() -> new FolioNotFoundException(command.folioId()));
		var method = payments.findMethodByCode(command.paymentMethodCode().trim())
				.orElseThrow(() -> new PaymentMethodNotFoundException(command.paymentMethodCode()));
		if (payments.existsByFolioId(folio.dbId()))
			throw new FolioAlreadyPaidException();
		folio.pay(command.amount());
		// ponytail: simulated gateway always approves; plug a real gateway client here.
		var reference = "SIM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
		var payment = payments.save(
				Payment.processed(folio.dbId(), method, command.amount(), reference, Instant.now(clock)));
		folios.save(folio);
		return payment;
	}

	@Override
	public Invoice issueInvoice(IssueInvoiceCommand command) {
		var payment = payments.findByPublicId(new PaymentId(command.paymentId()))
				.orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));
		if (invoices.existsByPaymentId(payment.dbId()))
			throw new InvoiceAlreadyIssuedException();
		var series = command.documentType().series();
		// ponytail: max+1 is racy under concurrent issuance; the UNIQUE(series, number) constraint rejects the loser.
		// Upgrade to a DB sequence per series or a row lock if issuance becomes concurrent.
		var next = invoices.findLastNumber(series).map(Long::parseLong).orElse(0L) + 1;
		return invoices.save(Invoice.issue(payment, command.documentType(), "%08d".formatted(next), Instant.now(clock)));
	}
}
