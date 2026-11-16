package com.swissotel.swissotel360.rooms.application.queryservices;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import java.math.BigDecimal;

public record RoomTypeView(RoomType roomType, BigDecimal currentPrice) {
}
