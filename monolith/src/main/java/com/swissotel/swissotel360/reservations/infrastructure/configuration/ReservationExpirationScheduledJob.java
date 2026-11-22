package com.swissotel.swissotel360.reservations.infrastructure.configuration;

import com.swissotel.swissotel360.reservations.application.internal.commandservices.ReservationCommandServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(name = "reservations.expiration-job.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationExpirationScheduledJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReservationExpirationScheduledJob.class);
	private final ReservationCommandServiceImpl commands;
	private final TransactionTemplate transactions;

	public ReservationExpirationScheduledJob(ReservationCommandServiceImpl commands, TransactionTemplate transactions) {
		this.commands = commands;
		this.transactions = transactions;
	}

	@Scheduled(fixedDelayString = "${reservations.expiration-job.fixed-delay-ms:60000}")
	public void expirePending() {
		for (var id : commands.findExpiredIds()) {
			try {
				transactions.executeWithoutResult(status -> commands.expire(id));
			} catch (RuntimeException ex) {
				LOGGER.error("Failed to expire pending reservation {}", id.value(), ex);
			}
		}
	}
}
