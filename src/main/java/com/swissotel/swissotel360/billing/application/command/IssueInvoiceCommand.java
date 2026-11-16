package com.swissotel.swissotel360.billing.application.command;

import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import java.util.UUID;

public record IssueInvoiceCommand(UUID paymentId, DocumentType documentType) {
}
