package com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl;

import com.swissotel.swissotel360.iam.interfaces.acl.IamContextFacade;
import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import com.swissotel.swissotel360.stays.interfaces.acl.StaysContextFacade;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/** Outbound adapter: the only place where service requests talk to other bounded contexts. */
@Component
public class ServiceRequestCollaboratorsOutboundService {
	private final StaysContextFacade stays;
	private final IamContextFacade iam;

	public ServiceRequestCollaboratorsOutboundService(StaysContextFacade stays, IamContextFacade iam) {
		this.stays = stays;
		this.iam = iam;
	}

	public Optional<StayContextSnapshot> findStay(UUID stayPublicId) {
		return stays.findByPublicId(stayPublicId);
	}

	public Optional<StayContextSnapshot> findStay(Long stayId) {
		return stays.findById(stayId);
	}

	/** Internal IAM id of the authenticated user, or null when it cannot be resolved. */
	public Long currentUserId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails principal))
			throw new IllegalStateException("An authenticated user is required");
		var id = iam.fetchUserIdByEmail(principal.getUsername());
		return id == null || id < 1 ? null : id;
	}
}
