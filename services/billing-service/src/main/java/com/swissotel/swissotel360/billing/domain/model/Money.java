package com.swissotel.swissotel360.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Monetary amounts are BigDecimal with exactly two decimals and never negative. */
public final class Money {
	private Money() {
	}

	public static BigDecimal of(BigDecimal value) {
		if (value == null || value.signum() < 0)
			throw new IllegalArgumentException("amount must be zero or positive");
		try {
			return value.setScale(2, RoundingMode.UNNECESSARY);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException("amount must have at most 2 decimals");
		}
	}
}
