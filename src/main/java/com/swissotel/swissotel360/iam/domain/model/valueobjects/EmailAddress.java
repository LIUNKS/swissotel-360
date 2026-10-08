package com.swissotel.swissotel360.iam.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Value Object representing a validated email address.
 */
public record EmailAddress(String address) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailAddress {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or blank");
        }
        String trimmed = address.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + address);
        }
        address = trimmed.toLowerCase();
    }

    @Override
    public String toString() {
        return address;
    }
}
