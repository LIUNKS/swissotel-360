package com.swissotel.swissotel360.servicerequests.interfaces.acl;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestsContextFacade {
	Optional<ServiceRequestContextSnapshot> findByPublicId(UUID requestPublicId);

	Optional<ServiceRequestContextSnapshot> findById(Long requestId);
}
