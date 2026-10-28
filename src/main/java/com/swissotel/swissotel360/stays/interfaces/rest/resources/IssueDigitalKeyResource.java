package com.swissotel.swissotel360.stays.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request to issue a new digital key for an active stay")
public record IssueDigitalKeyResource(@NotNull UUID stayId) {
}
