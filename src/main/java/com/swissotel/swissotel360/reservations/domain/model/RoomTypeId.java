package com.swissotel.swissotel360.reservations.domain.model;

public record RoomTypeId(Long value) {
    public RoomTypeId { if (value == null || value < 1) throw new IllegalArgumentException("room type id must be positive"); }
}
