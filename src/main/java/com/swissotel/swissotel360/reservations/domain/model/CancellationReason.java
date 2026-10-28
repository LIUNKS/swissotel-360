package com.swissotel.swissotel360.reservations.domain.model;

public enum CancellationReason {
    GUEST_REQUEST, STAFF, PAYMENT_TIMEOUT;
    public String code() { return name().toLowerCase(java.util.Locale.ROOT); }
    public static CancellationReason fromCode(String code) { return valueOf(code.toUpperCase(java.util.Locale.ROOT)); }
}
