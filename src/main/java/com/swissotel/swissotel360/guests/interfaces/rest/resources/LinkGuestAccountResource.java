package com.swissotel.swissotel360.guests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(name = "LinkGuestAccountRequest", description = "IAM account to associate with an existing guest")
public record LinkGuestAccountResource(
        @Schema(example = "b1398429-d78b-4f7b-be43-bb3fd351bc68") @NotNull UUID userPublicId) { }
