package com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl;

import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;

import java.util.Optional;
import java.util.UUID;

/** Outbound port: the only place where service requests query collaborator contexts. */
public interface ServiceRequestCollaboratorsOutboundService {

	Optional<StayContextSnapshot> findStay(UUID stayPublicId);

	Optional<StayContextSnapshot> findStay(Long stayId);

	boolean isCurrentUserGuest();

	Optional<Long> currentGuestId();

	Long currentUserId();
}
