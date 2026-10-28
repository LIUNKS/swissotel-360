package com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.assemblers;

import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;
import com.swissotel.swissotel360.stays.domain.model.KeyStatus;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities.DigitalKeyPersistenceEntity;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities.StayPersistenceEntity;
import java.util.List;

public final class StayPersistenceAssembler {
	private StayPersistenceAssembler() {
	}

	public static Stay toDomain(StayPersistenceEntity e, List<DigitalKeyPersistenceEntity> keys) {
		return Stay.restore(new StayId(e.getPublicId()), e.getId(), e.getReservationId(), e.getRoomId(),
				e.getGuestId(), e.getCheckedInAt(), e.getCheckedOutAt(), StayStatus.fromCode(e.getStatus()),
				e.getCheckedInBy(), keys.stream().map(StayPersistenceAssembler::toDomain).toList());
	}

	public static DigitalKey toDomain(DigitalKeyPersistenceEntity e) {
		return DigitalKey.restore(e.getId(), e.getAccessCode(), e.getActivatedAt(), e.getRevokedAt(),
				KeyStatus.fromCode(e.getStatus()));
	}

	public static StayPersistenceEntity toEntity(Stay a, StayPersistenceEntity e) {
		e.setPublicId(a.id().value());
		e.setReservationId(a.reservationId());
		e.setRoomId(a.roomId());
		e.setGuestId(a.guestId());
		e.setCheckedInAt(a.checkedInAt());
		e.setCheckedOutAt(a.checkedOutAt());
		e.setStatus(a.status().code());
		e.setCheckedInBy(a.checkedInBy());
		return e;
	}

	public static DigitalKeyPersistenceEntity toEntity(DigitalKey k, Long stayId, DigitalKeyPersistenceEntity e) {
		e.setStayId(stayId);
		e.setAccessCode(k.accessCode());
		e.setActivatedAt(k.activatedAt());
		e.setRevokedAt(k.revokedAt());
		e.setStatus(k.status().code());
		return e;
	}
}
