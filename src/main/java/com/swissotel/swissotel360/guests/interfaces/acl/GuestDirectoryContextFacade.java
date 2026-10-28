package com.swissotel.swissotel360.guests.interfaces.acl;

import java.util.Optional;
import java.util.UUID;

public interface GuestDirectoryContextFacade {
	Optional<GuestDirectorySnapshot> findByPublicId(UUID publicId);

	Optional<GuestDirectorySnapshot> findById(Long id);

	Optional<GuestDirectorySnapshot> findByUserId(Long userId);
}
