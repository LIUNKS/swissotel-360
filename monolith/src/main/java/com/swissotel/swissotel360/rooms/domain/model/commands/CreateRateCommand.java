package com.swissotel.swissotel360.rooms.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRateCommand(UUID roomTypePublicId, BigDecimal price, String season, LocalDate validFrom,
		LocalDate validTo) {
}
