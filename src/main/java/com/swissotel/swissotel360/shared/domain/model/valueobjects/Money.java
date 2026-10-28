package com.swissotel.swissotel360.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Immutable monetary amount used by bounded contexts that share the hotel currency (PEN). */
public record Money(BigDecimal amount) {
    public Money {
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() < 0) throw new IllegalArgumentException("amount cannot be negative");
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money zero() { return new Money(BigDecimal.ZERO.setScale(2)); }
    public Money plus(Money other) { return new Money(amount.add(other.amount)); }
}
