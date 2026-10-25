package com.swissotel.swissotel360.guests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateGuestRequest", description = "Editable guest profile fields; identity document is immutable")
public record UpdateGuestResource(
        @Schema(example = "Ana") @NotBlank @Size(max = 100) String firstName,
        @Schema(example = "García") @NotBlank @Size(max = 100) String lastName,
        @Schema(example = "ana@example.com") @Email @Size(max = 150) String email,
        @Schema(example = "+51987654321") @Size(max = 30) String phone,
        @Schema(example = "Peru") @Size(max = 60) String countryOfOrigin) { }
