package com.swissotel.swissotel360.billing.infrastructure.http;

import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.ServiceRequestCollaboratorSnapshot;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.StayCollaboratorSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange
public interface OperationsInternalClient {

	@GetExchange("/internal/stays/{stayId}")
	StayCollaboratorSnapshot getStayById(@PathVariable("stayId") Long stayId);

	@GetExchange("/internal/stays/by-public-id/{stayPublicId}")
	StayCollaboratorSnapshot getStayByPublicId(@PathVariable("stayPublicId") UUID stayPublicId);

	@GetExchange("/internal/service-requests/{requestId}")
	ServiceRequestCollaboratorSnapshot getServiceRequestById(@PathVariable("requestId") Long requestId);

	@GetExchange("/internal/service-requests/by-public-id/{requestPublicId}")
	ServiceRequestCollaboratorSnapshot getServiceRequestByPublicId(
			@PathVariable("requestPublicId") UUID requestPublicId);
}
