package com.swissotel.swissotel360.guests.domain.model.valueobjects;

import com.swissotel.swissotel360.guests.domain.exceptions.InvalidDocumentNumberException;
import java.util.Locale;

/** Validates an identity document number according to its document type. */
public record IdentityDocumentNumber(String typeCode, String value) {
    public IdentityDocumentNumber {
        if (typeCode == null || value == null || value.isBlank()) {
            throw new InvalidDocumentNumberException("Document type and number are required");
        }
        typeCode = typeCode.trim().toUpperCase(Locale.ROOT);
        value = value.trim();
        boolean valid = switch (typeCode) {
            case "DNI" -> value.matches("[0-9]{8}");
            case "CE", "PASSPORT" -> value.matches("[A-Za-z0-9]{1,20}");
            default -> false;
        };
        if (!valid) {
            throw new InvalidDocumentNumberException("Document number format is invalid for " + typeCode);
        }
    }
}
