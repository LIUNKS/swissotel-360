package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.assemblers;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities.GuestPersistenceEntity;

public final class GuestPersistenceAssembler {
    private GuestPersistenceAssembler() { }

    public static Guest toDomain(GuestPersistenceEntity entity) {
        if (entity == null) return null;
        return new Guest(entity.getId(), entity.getPublicId(), entity.getUserId(), entity.getDocumentType().getId(),
                entity.getDocumentType().getCode(), entity.getDocumentNumber(), entity.getFirstName(), entity.getLastName(),
                entity.getEmail(), entity.getPhone(), entity.getCountryOfOrigin(), entity.getRegisteredAt());
    }

    public static GuestPersistenceEntity toPersistence(Guest guest, GuestPersistenceEntity entity,
            com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities.DocumentTypePersistenceEntity documentType) {
        entity.setPublicId(guest.getPublicId());
        entity.setUserId(guest.getUserId());
        entity.setDocumentType(documentType);
        entity.setDocumentNumber(guest.getDocumentNumber());
        entity.setFirstName(guest.getFirstName());
        entity.setLastName(guest.getLastName());
        entity.setEmail(guest.getEmail());
        entity.setPhone(guest.getPhone());
        entity.setCountryOfOrigin(guest.getCountryOfOrigin());
        entity.setRegisteredAt(guest.getRegisteredAt());
        return entity;
    }
}
