package com.swissotel.swissotel360.rooms.application.queryservices;

import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import java.util.UUID;

public interface RoomQueryService {
	RoomPageView find(RoomFilter filter);

	RoomPageView find(UUID roomTypePublicId, RoomStatus status, int page, int size);

	RoomView findByPublicId(GetRoomByPublicIdQuery query);

	java.util.Optional<RoomView> findById(Long id);
}
