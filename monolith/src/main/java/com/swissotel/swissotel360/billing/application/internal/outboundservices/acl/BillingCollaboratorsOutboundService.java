package com.swissotel.swissotel360.billing.application.internal.outboundservices.acl;

import com.swissotel.swissotel360.servicerequests.interfaces.acl.ServiceRequestContextSnapshot;
import com.swissotel.swissotel360.servicerequests.interfaces.acl.ServiceRequestsContextFacade;
import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import com.swissotel.swissotel360.stays.interfaces.acl.StaysContextFacade;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/** Outbound adapter: the only place where billing talks to other bounded contexts. */
@Component
public class BillingCollaboratorsOutboundService {
	private final StaysContextFacade stays;
	private final ServiceRequestsContextFacade serviceRequests;

	public BillingCollaboratorsOutboundService(StaysContextFacade stays, ServiceRequestsContextFacade serviceRequests) {
		this.stays = stays;
		this.serviceRequests = serviceRequests;
	}

	public Optional<StayContextSnapshot> findStay(UUID stayPublicId) {
		return stays.findByPublicId(stayPublicId);
	}

	public Optional<StayContextSnapshot> findStay(Long stayId) {
		return stays.findById(stayId);
	}

	public Optional<ServiceRequestContextSnapshot> findServiceRequest(Long requestId) {
		return serviceRequests.findById(requestId);
	}

	public Optional<ServiceRequestContextSnapshot> findServiceRequest(UUID requestPublicId) {
		return serviceRequests.findByPublicId(requestPublicId);
	}
}