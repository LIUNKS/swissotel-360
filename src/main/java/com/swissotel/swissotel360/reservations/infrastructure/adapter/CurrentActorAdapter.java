package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentActorAdapter implements CurrentActorPort {
	@Override
	public CurrentActor current() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal))
			throw new IllegalStateException("An authenticated user is required");
		return new CurrentActor(new UserId(principal.userId()), principal.role(), principal.permissions());
	}
}
