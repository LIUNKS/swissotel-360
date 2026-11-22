package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Price for one night")
public record NightPriceResource(LocalDate date, BigDecimal price) {
}
