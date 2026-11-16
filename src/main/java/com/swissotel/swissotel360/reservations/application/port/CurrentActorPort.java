package com.swissotel.swissotel360.reservations.application.port;

import com.swissotel.swissotel360.reservations.domain.model.UserId;
import java.util.Set;

public interface CurrentActorPort {
	CurrentActor current();

	record CurrentActor(UserId userId, String roleCode, Set<String> permissions) {
		public CurrentActor {
			permissions = Set.copyOf(permissions);
		}

		public boolean isGuest() {
			return "ROLE_GUEST".equals(roleCode);
		}

		public boolean can(String permission) {
			return permissions.contains(permission);
		}
	}
}
