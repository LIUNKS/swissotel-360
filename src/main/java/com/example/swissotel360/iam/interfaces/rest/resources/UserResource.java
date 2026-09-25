package com.example.swissotel360.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Represents a user in the system")
public record UserResource(
                @Schema(description = "Internal database identifier", example = "1") @jakarta.validation.constraints.NotNull Long id,

                @Schema(description = "Public UUID identifier", example = "b1398429-d78b-4f7b-be43-bb3fd351bc68") @jakarta.validation.constraints.NotNull UUID publicId,

                @Schema(description = "Full name of the user", example = "John Doe") @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 120) String fullName,

                @Schema(description = "User email", example = "john.doe@example.com") @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email @jakarta.validation.constraints.Size(max = 150) String email,

                @Schema(description = "Assigned role", example = "ROLE_GUEST") @jakarta.validation.constraints.NotBlank String role) {
}
