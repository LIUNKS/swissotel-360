package com.swissotel.swissotel360.guests.domain.model.commands;

import java.util.UUID;

public record LinkGuestAccountCommand(UUID guestPublicId, UUID userPublicId) {
}
