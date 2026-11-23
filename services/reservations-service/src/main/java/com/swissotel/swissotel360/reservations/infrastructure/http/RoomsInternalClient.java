package com.swissotel.swissotel360.reservations.infrastructure.http;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.UUID;

@HttpExchange
public interface RoomsInternalClient {

	@GetExchange("/internal/room-types/{roomTypeId}")
	RoomTypeCollaboratorSnapshot getRoomTypeById(@PathVariable("roomTypeId") Long roomTypeId);

	@GetExchange("/internal/room-types/by-public-id/{roomTypePublicId}")
	RoomTypeCollaboratorSnapshot getRoomTypeByPublicId(@PathVariable("roomTypePublicId") UUID roomTypePublicId);

	@GetExchange("/internal/room-types/{roomTypeId}/sellable-units")
	SellableUnitsResponse getSellableUnits(@PathVariable("roomTypeId") Long roomTypeId);

	@GetExchange("/internal/room-types/{roomTypeId}/quote")
	RoomQuoteCollaboratorSnapshot quote(
			@PathVariable("roomTypeId") Long roomTypeId,
			@RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
			@RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut);
}
