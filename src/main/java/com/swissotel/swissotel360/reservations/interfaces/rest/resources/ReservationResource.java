package com.swissotel.swissotel360.reservations.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description="Reservation details")
public record ReservationResource(UUID publicId,String reservationCode,String status,Guest guest,RoomType roomType,
        LocalDate checkInDate,LocalDate checkOutDate,int nights,String paymentType,BigDecimal totalAmount,
        List<NightPrice> priceBreakdown,Instant holdExpiresAt,Instant createdAt,Prepayment prepayment) {
    public ReservationResource { priceBreakdown=List.copyOf(priceBreakdown); }
    @Schema(description="Guest associated with the reservation") public record Guest(UUID publicId,String fullName) { }
    @Schema(description="Reserved room type") public record RoomType(UUID publicId,String name) { }
    @Schema(description="Price charged for one night") public record NightPrice(LocalDate date,BigDecimal price) { }
    @Schema(description="Prepayment workflow state") public record Prepayment(String status,String message) { }
}
