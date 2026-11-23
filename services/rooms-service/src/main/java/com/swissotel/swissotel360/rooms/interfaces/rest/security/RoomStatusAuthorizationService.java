package com.swissotel.swissotel360.rooms.interfaces.rest.security;

import com.swissotel.swissotel360.rooms.application.queryservices.RoomQueryService;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("roomStatusAuthorizationService")
public class RoomStatusAuthorizationService {
	private final RoomQueryService rooms;

	public RoomStatusAuthorizationService(RoomQueryService rooms) {
		this.rooms = rooms;
	}

	public boolean canUpdate(Authentication authentication, UUID roomPublicId, RoomStatus nextStatus) {
		var currentStatus = rooms.findByPublicId(new GetRoomByPublicIdQuery(roomPublicId)).room().status();
		var hasManagerPermission = hasAuthority(authentication, "rooms.manage");
		var hasStatusPermission = hasAuthority(authentication, "rooms.update_status");
		var isHousekeeping = hasAuthority(authentication, "ROLE_HOUSEKEEPING");
		if (currentStatus == RoomStatus.OCCUPIED && nextStatus == RoomStatus.CLEANING)
			return false;
		if (nextStatus == RoomStatus.OUT_OF_SERVICE || currentStatus == RoomStatus.OUT_OF_SERVICE)
			return hasManagerPermission;
		if (nextStatus == RoomStatus.AVAILABLE && currentStatus == RoomStatus.CLEANING)
			return hasManagerPermission || isHousekeeping;
		return hasManagerPermission || hasStatusPermission;
	}

	private boolean hasAuthority(Authentication authentication, String authority) {
		return authentication.getAuthorities().stream().anyMatch(granted -> granted.getAuthority().equals(authority));
	}
}
