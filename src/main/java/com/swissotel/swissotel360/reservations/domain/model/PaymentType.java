package com.swissotel.swissotel360.reservations.domain.model;

public enum PaymentType {
    IMMEDIATE, DEFERRED;
    public String code() { return name().toLowerCase(java.util.Locale.ROOT); }
    public static PaymentType fromCode(String code) { return valueOf(code.toUpperCase(java.util.Locale.ROOT)); }
}
