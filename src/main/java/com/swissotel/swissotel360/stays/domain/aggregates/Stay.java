package com.swissotel.swissotel360.stays.domain.aggregates;

import com.swissotel.swissotel360.stays.domain.events.StayCheckedIn;
import com.swissotel.swissotel360.stays.domain.events.StayCheckedOut;
import com.swissotel.swissotel360.stays.domain.exceptions.DigitalKeyNotFoundException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayAlreadyClosedException;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Stay extends AbstractDomainAggregateRoot<Stay> {
	private final StayId id;
	private final Long dbId;
	private final Long reservationId;
	private final Long roomId;
	private final Long guestId;
	private final Instant checkedInAt;
	private Instant checkedOutAt;
	private StayStatus status;
	private final Long checkedInBy;
	private final List<DigitalKey> keys;

	private Stay(StayId id, Long dbId, Long reservationId, Long roomId, Long guestId, Instant checkedInAt,
			Instant checkedOutAt, StayStatus status, Long checkedInBy, List<DigitalKey> keys) {
		if (id == null || reservationId == null || roomId == null || guestId == null || checkedInAt == null
				|| status == null)
			throw new IllegalArgumentException("Stay fields are required");
		this.id = id;
		this.dbId = dbId;
		this.reservationId = reservationId;
		this.roomId = roomId;
		this.guestId = guestId;
		this.checkedInAt = checkedInAt;
		this.checkedOutAt = checkedOutAt;
		this.status = status;
		this.checkedInBy = checkedInBy;
		this.keys = new ArrayList<>(keys);
	}

	/** Opens a stay and auto-issues its first digital key. */
	public static Stay checkIn(Long reservationId, Long roomId, Long guestId, Long checkedInBy, Instant now) {
		var stay = new Stay(StayId.newId(), null, reservationId, roomId, guestId, now, null, StayStatus.ACTIVE,
				checkedInBy, List.of(DigitalKey.issue(now)));
		stay.registerDomainEvent(new StayCheckedIn(stay.id, reservationId, guestId, roomId));
		return stay;
	}

	public static Stay restore(StayId id, Long dbId, Long reservationId, Long roomId, Long guestId,
			Instant checkedInAt, Instant checkedOutAt, StayStatus status, Long checkedInBy, List<DigitalKey> keys) {
		return new Stay(id, dbId, reservationId, roomId, guestId, checkedInAt, checkedOutAt, status, checkedInBy,
				keys);
	}

	public void checkOut(Instant now) {
		requireActive();
		status = StayStatus.CLOSED;
		checkedOutAt = now;
		keys.forEach(k -> k.revoke(now));
		registerDomainEvent(new StayCheckedOut(id, guestId, roomId));
	}

	/** Revokes the current active key (if any) and issues a new one. */
	public DigitalKey issueKey(Instant now) {
		requireActive();
		keys.forEach(k -> k.revoke(now));
		var key = DigitalKey.issue(now);
		keys.add(key);
		return key;
	}

	public void revokeKey(Long keyId, Instant now) {
		keys.stream().filter(k -> keyId.equals(k.id())).findFirst()
				.orElseThrow(() -> new DigitalKeyNotFoundException(keyId)).revoke(now);
	}

	public boolean isActive() {
		return status == StayStatus.ACTIVE;
	}

	public Optional<DigitalKey> activeKey() {
		return keys.stream().filter(DigitalKey::isActive).findFirst();
	}

	private void requireActive() {
		if (status != StayStatus.ACTIVE)
			throw new StayAlreadyClosedException(id.value());
	}

	public StayId id() {
		return id;
	}

	public Long dbId() {
		return dbId;
	}

	public Long reservationId() {
		return reservationId;
	}

	public Long roomId() {
		return roomId;
	}

	public Long guestId() {
		return guestId;
	}

	public Instant checkedInAt() {
		return checkedInAt;
	}

	public Instant checkedOutAt() {
		return checkedOutAt;
	}

	public StayStatus status() {
		return status;
	}

	public Long checkedInBy() {
		return checkedInBy;
	}

	public List<DigitalKey> keys() {
		return Collections.unmodifiableList(keys);
	}
}
