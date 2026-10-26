package com.swissotel.swissotel360.guests.interfaces.rest.transform;

import com.swissotel.swissotel360.guests.domain.model.commands.CreateGuestCommand;
import com.swissotel.swissotel360.guests.interfaces.rest.resources.CreateGuestResource;

public final class CreateGuestCommandAssembler {
    private CreateGuestCommandAssembler() { }

    public static CreateGuestCommand toCommand(CreateGuestResource resource) {
        return new CreateGuestCommand(resource.documentTypeId(), resource.documentNumber(), resource.firstName(),
                resource.lastName(), resource.email(), resource.phone(), resource.countryOfOrigin());
    }
}
