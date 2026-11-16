package com.swissotel.swissotel360.billing.application.dto;

import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import java.util.Map;
import java.util.UUID;

/** serviceRequestPublicIds maps a charge's internal service request id to its public id. */
public record FolioView(Folio folio, UUID stayPublicId, Map<Long, UUID> serviceRequestPublicIds) {
}