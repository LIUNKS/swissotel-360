package com.swissotel.swissotel360.stays.interfaces.acl;

import java.util.Optional;
import java.util.UUID;

public interface StaysContextFacade {
	Optional<StayContextSnapshot> findByPublicId(UUID stayPublicId);

	Optional<StayContextSnapshot> findById(Long stayId);

	boolean isActive(UUID stayPublicId);
}
