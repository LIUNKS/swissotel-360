package com.swissotel.swissotel360.reservations.domain.services;

import com.swissotel.swissotel360.reservations.domain.exceptions.NoAvailabilityException;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;

public final class AvailabilityPolicy {
	public void assertAvailable(int sellableUnits, long activeOverlapping, RoomTypeId type) {
		if (sellableUnits < 0 || activeOverlapping < 0)
			throw new IllegalArgumentException("Inventory counts cannot be negative");
		if (sellableUnits - activeOverlapping <= 0)
			throw new NoAvailabilityException(type);
	}
}
