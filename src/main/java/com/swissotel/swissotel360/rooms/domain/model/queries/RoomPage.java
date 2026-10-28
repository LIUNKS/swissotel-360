package com.swissotel.swissotel360.rooms.domain.model.queries;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import java.util.List;
public record RoomPage(List<Room> content, long totalElements) { public RoomPage { content = List.copyOf(content); } }
