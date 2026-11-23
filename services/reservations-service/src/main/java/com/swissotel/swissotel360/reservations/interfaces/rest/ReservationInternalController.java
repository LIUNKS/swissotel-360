package com.swissotel.swissotel360.reservations.interfaces.rest;

import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationContextSnapshot;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Internal service-to-service endpoint exposed for downstream microservices (blocked at API Gateway). */
@Hidden
@RestController
@RequestMapping("/internal/reservations")
public class ReservationInternalController {

	private final ReservationsContextFacade reservations;

	public ReservationInternalController(ReservationsContextFacade reservations) {
		this.reservations = reservations;
	}

	@GetMapping("/availability/overlapping")
	public ResponseEntity<OverlappingCountResponse> countOverlapping(
			@RequestParam Long roomTypeId,
			@RequestParam LocalDate checkIn,
			@RequestParam LocalDate checkOut) {
		return ResponseEntity.ok(new OverlappingCountResponse(
				reservations.countActiveReservationsOverlapping(roomTypeId, checkIn, checkOut)));
	}

	@GetMapping("/exists-by-room-type")
	public ResponseEntity<RoomTypeReservationsResponse> hasReservations(@RequestParam Long roomTypeId) {
		return ResponseEntity.ok(new RoomTypeReservationsResponse(
				reservations.hasReservationsForRoomType(roomTypeId)));
	}

	@GetMapping("/by-code/{code}")
	public ResponseEntity<ReservationContextSnapshot> findByCode(@PathVariable String code) {
		return reservations.findByCode(code).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/{reservationId}/complete")
	public ResponseEntity<Void> markCompleted(@PathVariable Long reservationId) {
		reservations.markCompleted(reservationId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{reservationId}/confirm-prepaid")
	public ResponseEntity<Void> confirmPrepaid(@PathVariable Long reservationId) {
		reservations.confirmPrepaid(reservationId);
		return ResponseEntity.noContent().build();
	}

	public record OverlappingCountResponse(long count) {
	}

	public record RoomTypeReservationsResponse(boolean hasReservations) {
	}
}
