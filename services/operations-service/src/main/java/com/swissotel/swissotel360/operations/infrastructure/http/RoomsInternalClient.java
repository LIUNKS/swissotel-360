package com.swissotel.swissotel360.operations.infrastructure.http;

import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayRoomSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

@HttpExchange
public interface RoomsInternalClient {

	@GetExchange("/internal/rooms/{roomId}")
	StayRoomSnapshot getById(@PathVariable("roomId") Long roomId);

	@GetExchange("/internal/rooms/by-public-id/{roomPublicId}")
	StayRoomSnapshot getByPublicId(@PathVariable("roomPublicId") UUID roomPublicId);

	@GetExchange("/internal/rooms/available")
	StayRoomSnapshot findAvailable(@RequestParam("roomTypeId") Long roomTypeId);

	@PostExchange("/internal/rooms/{roomId}/occupy")
	RoomStatusSnapshot occupy(@PathVariable("roomId") Long roomId);

	@PostExchange("/internal/rooms/{roomId}/check-out")
	RoomStatusSnapshot checkOut(@PathVariable("roomId") Long roomId);

	@PostExchange("/internal/rooms/{roomId}/release")
	RoomStatusSnapshot release(@PathVariable("roomId") Long roomId);

	record RoomStatusSnapshot(UUID publicId, String status) {
	}
}
