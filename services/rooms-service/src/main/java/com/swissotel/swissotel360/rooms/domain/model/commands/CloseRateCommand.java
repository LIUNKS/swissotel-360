package com.swissotel.swissotel360.rooms.domain.model.commands;

import java.time.LocalDate;
import java.util.UUID;

public record CloseRateCommand(UUID roomTypePublicId, UUID ratePublicId, LocalDate validTo) {
}
