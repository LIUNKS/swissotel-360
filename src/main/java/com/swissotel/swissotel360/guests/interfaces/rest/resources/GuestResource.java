package com.swissotel.swissotel360.guests.interfaces.rest.resources;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "Guest", description = "Public guest representation")
public record GuestResource(
        @Schema(example = "c3150b31-ec24-4c2b-9707-60c6a3b304a3") UUID publicId,
        @Schema(example = "DNI") String documentTypeCode,
        @Schema(example = "12345678") String documentNumber,
        @Schema(example = "Ana") String firstName,
        @Schema(example = "García") String lastName,
        @Schema(example = "ana@example.com") String email,
        @Schema(example = "+51987654321") String phone,
        @Schema(example = "Peru") String countryOfOrigin,
        @Schema(example = "2026-10-25T14:30:00-05:00") OffsetDateTime registeredAt) {
    public static GuestResource from(Guest guest) {
        return new GuestResource(guest.getPublicId(), guest.getDocumentTypeCode(), guest.getDocumentNumber(),
                guest.getFirstName(), guest.getLastName(), guest.getEmail(), guest.getPhone(),
                guest.getCountryOfOrigin(), guest.getRegisteredAt());
    }
}
