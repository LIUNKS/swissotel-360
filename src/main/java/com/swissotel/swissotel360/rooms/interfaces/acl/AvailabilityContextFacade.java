package com.swissotel.swissotel360.rooms.interfaces.acl;

import java.time.LocalDate;
import java.util.UUID;

public interface AvailabilityContextFacade {
	int countAvailable(UUID roomTypePublicId, LocalDate checkIn, LocalDate checkOut);

	RoomAvailabilitySnapshot quote(UUID roomTypePublicId, LocalDate checkIn, LocalDate checkOut);

	java.util.Optional<RoomTypeInventorySnapshot> findRoomType(UUID roomTypePublicId);

	java.util.Optional<RoomTypeInventorySnapshot> findRoomTypeById(Long roomTypeId);

	void lockInventory(Long roomTypeId);

	int sellableUnits(Long roomTypeId);

	RoomAvailabilitySnapshot quote(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}
