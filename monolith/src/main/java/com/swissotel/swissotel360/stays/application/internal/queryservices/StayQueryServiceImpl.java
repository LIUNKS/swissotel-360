package com.swissotel.swissotel360.stays.application.internal.queryservices;

import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayCollaboratorsOutboundService;
import com.swissotel.swissotel360.stays.application.queryservices.StayQueryService;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.exceptions.StayNotFoundException;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.domain.repositories.StayRepository;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomInfoSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class StayQueryServiceImpl implements StayQueryService {
	private final StayRepository stays;
	private final StayCollaboratorsOutboundService collaborators;

	public StayQueryServiceImpl(StayRepository stays, StayCollaboratorsOutboundService collaborators) {
		this.stays = stays;
		this.collaborators = collaborators;
	}

	@Override
	public StayView getView(UUID id) {
		return view(findByPublicId(id).orElseThrow(() -> new StayNotFoundException(id)));
	}

	@Override
	public List<StayView> list(StayStatus status) {
		return stays.findAll(status).stream().map(this::view).toList();
	}

	@Override
	public Optional<Stay> findByPublicId(UUID id) {
		return stays.findByPublicId(new StayId(id));
	}

	@Override
	public Optional<Stay> findById(Long id) {
		return stays.findById(id);
	}

	private StayView view(Stay stay) {
		var room = collaborators.findRoom(stay.roomId());
		return new StayView(stay, room.map(RoomInfoSnapshot::publicId).orElse(null),
				room.map(RoomInfoSnapshot::roomNumber).orElse(null));
	}
}
