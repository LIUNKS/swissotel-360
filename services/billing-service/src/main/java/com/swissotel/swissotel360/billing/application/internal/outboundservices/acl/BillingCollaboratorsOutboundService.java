package com.swissotel.swissotel360.billing.application.internal.outboundservices.acl;

import java.util.Optional;
import java.util.UUID;

/** Outbound port: the only place where billing queries collaborator services. */
public interface BillingCollaboratorsOutboundService {

	Optional<StayCollaboratorSnapshot> findStay(UUID stayPublicId);

	Optional<StayCollaboratorSnapshot> findStay(Long stayId);

	Optional<ServiceRequestCollaboratorSnapshot> findServiceRequest(Long requestId);

	Optional<ServiceRequestCollaboratorSnapshot> findServiceRequest(UUID requestPublicId);
}
