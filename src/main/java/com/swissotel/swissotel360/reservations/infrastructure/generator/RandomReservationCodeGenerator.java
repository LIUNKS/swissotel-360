package com.swissotel.swissotel360.reservations.infrastructure.generator;

import com.swissotel.swissotel360.reservations.domain.model.ReservationCode;
import com.swissotel.swissotel360.reservations.domain.services.ReservationCodeGenerator;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class RandomReservationCodeGenerator implements ReservationCodeGenerator {
	private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
	private final SecureRandom random = new SecureRandom();
	private final Clock clock;

	public RandomReservationCodeGenerator(Clock clock) {
		this.clock = clock;
	}

	@Override
	public ReservationCode next() {
		var builder = new StringBuilder("SW")
				.append(String.format("%02d", LocalDate.now(clock).getYear() % 100))
				.append('-');
		for (int i = 0; i < 8; i++) {
			builder.append(ALPHABET[random.nextInt(ALPHABET.length)]);
		}
		return new ReservationCode(builder.toString());
	}
}
