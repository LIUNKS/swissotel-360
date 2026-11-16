package com.swissotel.swissotel360.billing.interfaces.rest.transform;

import com.swissotel.swissotel360.billing.application.command.*;
import com.swissotel.swissotel360.billing.application.dto.FolioView;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import com.swissotel.swissotel360.billing.interfaces.rest.resources.*;
import java.util.UUID;

public final class BillingRestMapper {
	private BillingRestMapper() {
	}

	public static AddChargeCommand toCommand(UUID folioId, AddChargeResource r) {
		return new AddChargeCommand(folioId, r.concept(), r.amount(), r.serviceRequestId());
	}

	public static ProcessPaymentCommand toCommand(CreatePaymentResource r) {
		return new ProcessPaymentCommand(r.folioId(), r.paymentMethodCode(), r.amount());
	}

	public static IssueInvoiceCommand toCommand(IssueInvoiceResource r) {
		return new IssueInvoiceCommand(r.paymentId(), DocumentType.fromCode(r.documentType().trim()));
	}

	public static FolioResource toResource(FolioView v) {
		var f = v.folio();
		return new FolioResource(f.id().value(), v.stayPublicId(), f.status().code(), f.total());
	}

	public static FolioSummaryResource toSummary(FolioView v) {
		var f = v.folio();
		return new FolioSummaryResource(f.id().value(), f.status().code(),
				f.charges().stream().map(c -> toResource(c, v)).toList(), f.total());
	}

	private static ChargeResource toResource(Charge c, FolioView v) {
		return new ChargeResource(c.dbId(), c.concept(), c.amount(), c.chargedAt(),
				c.serviceRequestId() == null ? null : v.serviceRequestPublicIds().get(c.serviceRequestId()));
	}

	public static ChargeResultResource toResult(Charge c) {
		return new ChargeResultResource(c.dbId(), c.concept(), c.amount(), c.chargedAt());
	}

	public static PaymentMethodResource toResource(PaymentMethod m) {
		return new PaymentMethodResource(m.code(), m.name());
	}

	public static PaymentResultResource toResult(Payment p) {
		return new PaymentResultResource(p.id().value(), p.status().code(), p.gatewayReference(), p.paidAt());
	}

	public static InvoiceResource toResource(Invoice i) {
		return new InvoiceResource(i.id().value(), i.documentType().code(), i.series(), i.number(),
				i.totalAmount(), i.issuedAt());
	}
}