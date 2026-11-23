package com.swissotel.swissotel360.stays.application.dto;

import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import java.util.UUID;

/** A stay enriched with the room data owned by the rooms context. */
public record StayView(Stay stay, UUID roomPublicId, String roomNumber) {
}
