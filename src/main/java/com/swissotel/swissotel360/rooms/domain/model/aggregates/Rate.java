package com.swissotel.swissotel360.rooms.domain.model.aggregates;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record Rate(Long id, Long roomTypeId, UUID roomTypePublicId, BigDecimal price, String season, LocalDate validFrom, LocalDate validTo) {
    public Rate {
        if (roomTypeId == null || roomTypePublicId == null || price == null || validFrom == null) throw new IllegalArgumentException("Room type, price and validFrom are required");
        if (price.signum() < 0 || price.compareTo(new BigDecimal("99999999.99")) > 0 || price.stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("price must fit NUMERIC(10,2) and be non-negative");
        if (season != null && season.length() > 30) throw new IllegalArgumentException("season cannot exceed 30 characters");
        if (validTo != null && validTo.isBefore(validFrom)) throw new IllegalArgumentException("validTo must be on or after validFrom");
    }
    public Rate close(LocalDate date) {
        if (validTo != null) throw new IllegalArgumentException("Rate is already closed");
        if (date == null || date.isBefore(validFrom)) throw new IllegalArgumentException("validTo must be on or after validFrom");
        return new Rate(id, roomTypeId, roomTypePublicId, price, season, validFrom, date);
    }
    public void validateDeletable(LocalDate today) { if (!validFrom.isAfter(today)) throw new com.swissotel.swissotel360.rooms.domain.exceptions.RateCannotBeDeletedException(id); }
}
