package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.assemblers;

import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.model.*;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.*;
import java.util.List;

public final class BillingPersistenceAssembler {
	private BillingPersistenceAssembler() {
	}

	public static Charge toDomain(ChargePersistenceEntity e) {
		return new Charge(e.getId(), e.getPublicId(), e.getServiceRequestId(), e.getConcept(), e.getAmount(),
				e.getChargedAt());
	}

	public static Folio toDomain(FolioPersistenceEntity e, List<ChargePersistenceEntity> charges) {
		return Folio.restore(new FolioId(e.getPublicId()), e.getId(), e.getStayId(), FolioStatus.fromCode(e.getStatus()),
				charges.stream().map(BillingPersistenceAssembler::toDomain).toList());
	}

	public static FolioPersistenceEntity toEntity(Folio a, FolioPersistenceEntity e) {
		e.setPublicId(a.id().value());
		e.setStayId(a.stayId());
		e.setStatus(a.status().code());
		e.setTotalAmount(a.total());
		return e;
	}

	public static ChargePersistenceEntity toEntity(Charge c, Long folioId) {
		var e = new ChargePersistenceEntity();
		e.setPublicId(c.publicId());
		e.setFolioId(folioId);
		e.setServiceRequestId(c.serviceRequestId());
		e.setConcept(c.concept());
		e.setAmount(c.amount());
		e.setChargedAt(c.chargedAt());
		return e;
	}

	public static PaymentMethod toDomain(PaymentMethodPersistenceEntity e) {
		return new PaymentMethod(e.getId(), e.getCode(), e.getName());
	}

	public static Payment toDomain(PaymentPersistenceEntity e, PaymentMethod method) {
		return Payment.restore(new PaymentId(e.getPublicId()), e.getId(), e.getFolioId(), method, e.getAmount(),
				PaymentStatus.fromCode(e.getStatus()), e.getGatewayReference(), e.getPaidAt());
	}

	public static PaymentPersistenceEntity toEntity(Payment a, PaymentPersistenceEntity e) {
		e.setPublicId(a.id().value());
		e.setFolioId(a.folioId());
		e.setPaymentMethodId(a.method().id());
		e.setAmount(a.amount());
		e.setStatus(a.status().code());
		e.setGatewayReference(a.gatewayReference());
		e.setPaidAt(a.paidAt());
		return e;
	}

	public static Invoice toDomain(InvoicePersistenceEntity e) {
		return Invoice.restore(new InvoiceId(e.getPublicId()), e.getId(), e.getPaymentId(),
				DocumentType.fromCode(e.getDocumentType()), e.getSeries(), e.getNumber(), e.getIssuedAt(),
				e.getTotalAmount());
	}

	public static InvoicePersistenceEntity toEntity(Invoice a, InvoicePersistenceEntity e) {
		e.setPublicId(a.id().value());
		e.setPaymentId(a.paymentId());
		e.setDocumentType(a.documentType().code());
		e.setSeries(a.series());
		e.setNumber(a.number());
		e.setIssuedAt(a.issuedAt());
		e.setTotalAmount(a.totalAmount());
		return e;
	}
}