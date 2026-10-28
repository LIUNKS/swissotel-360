package com.swissotel.swissotel360.stays.application.command;

import jakarta.validation.constraints.NotNull;

public record RevokeDigitalKeyCommand(@NotNull Long keyId) {
}
