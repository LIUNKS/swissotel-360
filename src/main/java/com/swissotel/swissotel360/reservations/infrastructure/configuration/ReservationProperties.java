package com.swissotel.swissotel360.reservations.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reservations")
public record ReservationProperties(long pendingHoldMinutes, int maxNights, ExpirationJob expirationJob) {
	public record ExpirationJob(boolean enabled, long fixedDelayMs) {
	}
}
