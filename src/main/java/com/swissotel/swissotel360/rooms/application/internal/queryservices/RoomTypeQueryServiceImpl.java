package com.swissotel.swissotel360.rooms.application.internal.queryservices;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomTypeQueryService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomTypeView;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RateRepository;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomTypeByPublicIdQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public class RoomTypeQueryServiceImpl implements RoomTypeQueryService {
    private final RoomTypeRepository roomTypes; private final RateRepository rates;
    public RoomTypeQueryServiceImpl(RoomTypeRepository roomTypes, RateRepository rates) { this.roomTypes=roomTypes; this.rates=rates; }
    public List<RoomTypeView> findAll() { return roomTypes.findAll().stream().map(t->new RoomTypeView(t,rates.findEffective(t.id(),LocalDate.now()).map(r->r.price()).orElse(null))).toList(); }
    public RoomTypeView findByPublicId(GetRoomTypeByPublicIdQuery query) { var id=query.publicId(); var type=roomTypes.findByPublicId(id).orElseThrow(()->new RoomTypeNotFoundException(id)); return new RoomTypeView(type,rates.findEffective(type.id(),LocalDate.now()).map(r->r.price()).orElse(null)); }
}
