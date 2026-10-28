package com.swissotel.swissotel360.rooms.application.internal.commandservices;
import com.swissotel.swissotel360.rooms.application.commandservices.RoomTypeCommandService;
import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomTypeException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeInUseException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RateRepository;
import com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl.ReservationsAvailabilityService;
import java.util.UUID;
public class RoomTypeCommandServiceImpl implements RoomTypeCommandService {
    private final RoomTypeRepository roomTypes; private final RoomRepository rooms; private final RateRepository rates; private final ReservationsAvailabilityService reservations;
    public RoomTypeCommandServiceImpl(RoomTypeRepository roomTypes, RoomRepository rooms, RateRepository rates, ReservationsAvailabilityService reservations) { this.roomTypes=roomTypes; this.rooms=rooms; this.rates=rates; this.reservations=reservations; }
    public RoomType create(CreateRoomTypeCommand c) { var name=c.name().trim(); if (roomTypes.existsByNameIgnoreCase(name)) throw new DuplicateRoomTypeException(name); return roomTypes.save(new RoomType(null, UUID.randomUUID(), name, c.capacity(), c.description())); }
    public RoomType update(UpdateRoomTypeCommand c) { var old=roomTypes.findByPublicId(c.publicId()).orElseThrow(()->new RoomTypeNotFoundException(c.publicId())); var name=c.name().trim(); if(roomTypes.existsByNameIgnoreCaseAndIdNot(name,old.id())) throw new DuplicateRoomTypeException(name); return roomTypes.save(old.update(name,c.capacity(),c.description())); }
    public void delete(DeleteRoomTypeCommand command) { var id=command.publicId(); var type=roomTypes.findByPublicId(id).orElseThrow(()->new RoomTypeNotFoundException(id)); if(rooms.countByRoomTypeId(type.id())>0 || rates.existsByRoomTypeId(type.id()) || reservations.hasReservations(type.publicId())) throw new RoomTypeInUseException(id); roomTypes.delete(type); }
}
