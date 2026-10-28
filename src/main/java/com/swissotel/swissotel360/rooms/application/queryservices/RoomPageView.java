package com.swissotel.swissotel360.rooms.application.queryservices;
import java.util.List;
public record RoomPageView(List<RoomView> content,long totalElements) { public RoomPageView { content=List.copyOf(content); } }
