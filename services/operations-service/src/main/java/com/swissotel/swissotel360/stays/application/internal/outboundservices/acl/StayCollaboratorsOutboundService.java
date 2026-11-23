package com.swissotel.swissotel360.stays.application.internal.outboundservices.acl;

import java.util.Optional;
import java.util.UUID;

/** Outbound port: the only place where stays talks to external collaborator services. */
public interface StayCollaboratorsOutboundService {

	Optional<StayReservationSnapshot> findReservation(String code);

	void completeReservation(Long reservationId);

	Optional<StayGuestSnapshot> findGuest(Long guestId);

	Optional<StayRoomSnapshot> findRoom(UUID roomPublicId);

	Optional<StayRoomSnapshot> findRoom(Long roomId);

	Optional<StayRoomSnapshot> findAvailableRoom(Long roomTypeId);

	void markRoomOccupied(Long roomId);

	void markRoomCheckedOut(Long roomId);

	void releaseRoom(Long roomId);

	/** Internal IAM id of the authenticated user. */
	Long currentUserId();
}
