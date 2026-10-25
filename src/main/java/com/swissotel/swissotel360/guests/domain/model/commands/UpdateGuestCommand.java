package com.swissotel.swissotel360.guests.domain.model.commands;

import java.util.UUID;

public record UpdateGuestCommand(UUID publicId, String firstName, String lastName,
        String email, String phone, String countryOfOrigin) { }
