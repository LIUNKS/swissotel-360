package com.swissotel.swissotel360.rooms.infrastructure.http;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;

@HttpExchange
public interface ReservationsInternalClient {

	@GetExchange("/internal/reservations/availability/overlapping")
	OverlappingCountResponse countOverlapping(
			@RequestParam("roomTypeId") Long roomTypeId,
			@RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
			@RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut);

	@GetExchange("/internal/reservations/exists-by-room-type")
	RoomTypeReservationsResponse hasReservations(@RequestParam("roomTypeId") Long roomTypeId);
}
