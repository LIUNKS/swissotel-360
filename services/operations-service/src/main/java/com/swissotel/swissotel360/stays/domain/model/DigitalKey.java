package com.swissotel.swissotel360.stays.domain.model;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/** Digital key entity owned by the {@code Stay} aggregate. */
public final class DigitalKey {
	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final Long id;
	private final UUID publicId;
	private final String accessCode;
	private final Instant activatedAt;
	private Instant revokedAt;
	private KeyStatus status;

	private DigitalKey(Long id, UUID publicId, String accessCode, Instant activatedAt, Instant revokedAt,
			KeyStatus status) {
		if (publicId == null || accessCode == null || accessCode.isBlank() || accessCode.length() > 64
				|| activatedAt == null || status == null)
			throw new IllegalArgumentException("Digital key fields are required");
		this.id = id;
		this.publicId = publicId;
		this.accessCode = accessCode;
		this.activatedAt = activatedAt;
		this.revokedAt = revokedAt;
		this.status = status;
	}

	public static DigitalKey issue(Instant now) {
		var code = new StringBuilder(12);
		for (int i = 0; i < 12; i++)
			code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		return new DigitalKey(null, UUID.randomUUID(), code.toString(), now, null, KeyStatus.ACTIVE);
	}

	public static DigitalKey restore(Long id, UUID publicId, String accessCode, Instant activatedAt, Instant revokedAt,
			KeyStatus status) {
		return new DigitalKey(id, publicId, accessCode, activatedAt, revokedAt, status);
	}

	public void revoke(Instant now) {
		if (status == KeyStatus.REVOKED)
			return;
		status = KeyStatus.REVOKED;
		revokedAt = now;
	}

	public boolean isActive() {
		return status == KeyStatus.ACTIVE;
	}

	public Long id() {
		return id;
	}

	public UUID publicId() {
		return publicId;
	}

	public String accessCode() {
		return accessCode;
	}

	public Instant activatedAt() {
		return activatedAt;
	}

	public Instant revokedAt() {
		return revokedAt;
	}

	public KeyStatus status() {
		return status;
	}
}
