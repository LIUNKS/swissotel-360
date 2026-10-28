package com.swissotel.swissotel360.rooms.application.queryservices;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record PriceQuote(UUID roomTypePublicId, LocalDate checkIn, LocalDate checkOut, int nights, BigDecimal totalAmount, List<NightPrice> breakdown) {
    public PriceQuote { breakdown = List.copyOf(breakdown); }
    public record NightPrice(LocalDate date, BigDecimal price) { }
}
