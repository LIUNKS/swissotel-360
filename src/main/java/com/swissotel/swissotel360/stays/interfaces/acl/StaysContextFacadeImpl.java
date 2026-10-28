package com.swissotel.swissotel360.stays.interfaces.acl;

import com.swissotel.swissotel360.stays.application.queryservices.StayQueryService;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class StaysContextFacadeImpl implements StaysContextFacade {
	private final StayQueryService stays;

	public StaysContextFacadeImpl(StayQueryService stays) {
		this.stays = stays;
	}

	@Override
	public Optional<StayContextSnapshot> findByPublicId(UUID id) {
		return stays.findByPublicId(id).map(StaysContextFacadeImpl::snapshot);
	}

	@Override
	public Optional<StayContextSnapshot> findById(Long id) {
		return stays.findById(id).map(StaysContextFacadeImpl::snapshot);
	}

	@Override
	public boolean isActive(UUID id) {
		return stays.findByPublicId(id).map(Stay::isActive).orElse(false);
	}

	private static StayContextSnapshot snapshot(Stay s) {
		return new StayContextSnapshot(s.dbId(), s.id().value(), s.reservationId(), s.guestId(), s.roomId(),
				s.status().code(), s.checkedInAt(), s.checkedOutAt());
	}
}
