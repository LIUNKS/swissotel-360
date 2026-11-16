package com.swissotel.swissotel360.guests.interfaces.rest.transform;

import com.swissotel.swissotel360.guests.domain.model.commands.UpdateGuestCommand;
import com.swissotel.swissotel360.guests.interfaces.rest.resources.UpdateGuestResource;
import java.util.UUID;

public final class GuestCommandAssembler {
	private GuestCommandAssembler() {
	}

	public static UpdateGuestCommand toCommand(UUID publicId, UpdateGuestResource resource) {
		return new UpdateGuestCommand(publicId, resource.firstName(), resource.lastName(), resource.email(),
				resource.phone(), resource.countryOfOrigin());
	}
}
