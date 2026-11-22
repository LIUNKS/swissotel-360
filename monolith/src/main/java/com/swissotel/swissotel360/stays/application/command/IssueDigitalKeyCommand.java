package com.swissotel.swissotel360.stays.application.command;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record IssueDigitalKeyCommand(@NotNull UUID stayPublicId) {
}
