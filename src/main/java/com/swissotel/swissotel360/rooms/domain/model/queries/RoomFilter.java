package com.swissotel.swissotel360.rooms.domain.model.queries;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
public record RoomFilter(Long roomTypeId, RoomStatus status, int page, int size) { }
