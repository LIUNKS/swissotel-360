package com.swissotel.swissotel360.servicerequests.application.dto;

import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import java.util.UUID;

/** A request enriched with the public id of its stay, which is owned by the stays context. */
public record ServiceRequestView(ServiceRequest request, UUID stayPublicId) {
}
