package com.swissotel.swissotel360.rooms.interfaces.acl;

import java.util.UUID;

public interface RoomsContextFacade {
	java.util.Optional<RoomInfoSnapshot> findByPublicId(UUID roomPublicId);

	java.util.Optional<RoomInfoSnapshot> findById(Long roomId);

	java.util.Optional<RoomInfoSnapshot> findFirstAvailable(Long roomTypeId);

	RoomStatusSnapshot markOccupied(UUID roomPublicId);

	RoomStatusSnapshot markCheckedOut(UUID roomPublicId);
}
