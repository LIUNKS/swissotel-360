package com.swissotel.swissotel360.rooms.interfaces.rest.resources;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description="Room type nightly rate and validity period")
public record RateResource(Long id,UUID roomTypePublicId,BigDecimal price,String season,LocalDate validFrom,LocalDate validTo) { }
