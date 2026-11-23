package com.swissotel.swissotel360.operations.infrastructure.http;

import com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl.ServiceRequestCollaboratorsOutboundService;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayGuestSnapshot;
import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import com.swissotel.swissotel360.stays.interfaces.acl.StaysContextFacade;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

@Component
public class HttpServiceRequestCollaboratorsAdapter implements ServiceRequestCollaboratorsOutboundService {

	private final StaysContextFacade stays;
	private final GuestsInternalClient guests;

	public HttpServiceRequestCollaboratorsAdapter(StaysContextFacade stays, GuestsInternalClient guests) {
		this.stays = stays;
		this.guests = guests;
	}

	@Override
	public Optional<StayContextSnapshot> findStay(UUID stayPublicId) {
		return stays.findByPublicId(stayPublicId);
	}

	@Override
	public Optional<StayContextSnapshot> findStay(Long stayId) {
		return stays.findById(stayId);
	}

	@Override
	public boolean isCurrentUserGuest() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_GUEST".equals(authority.getAuthority()));
	}

	@Override
	public Optional<Long> currentGuestId() {
		var userId = currentUserId();
		if (userId == null) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(guests.getByUserId(userId)).map(StayGuestSnapshot::id);
		} catch (HttpClientErrorException.NotFound ignored) {
			return Optional.empty();
		}
	}

	@Override
	public Long currentUserId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal))
			throw new IllegalStateException("An authenticated user is required");
		return principal.userId();
	}
}
