package com.swissotel.swissotel360.billing.application.port;

import java.time.Instant;

/** Port for tracking processed integration events to ensure at-least-once consumer idempotency. */
public interface ProcessedEventStore {

	boolean exists(String eventKey);

	void markProcessed(String eventKey, String eventType, Instant processedAt);
}
