package com.swissotel.swissotel360.rooms.interfaces.acl;

import java.util.UUID;

public interface RoomsContextFacade {
	RoomStatusSnapshot markOccupied(UUID roomPublicId);

	RoomStatusSnapshot markCheckedOut(UUID roomPublicId);
}
