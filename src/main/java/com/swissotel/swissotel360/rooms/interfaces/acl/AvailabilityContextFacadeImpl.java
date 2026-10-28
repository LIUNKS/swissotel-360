package com.swissotel.swissotel360.rooms.interfaces.acl;
import com.swissotel.swissotel360.rooms.application.queryservices.AvailabilityQueryService;
import com.swissotel.swissotel360.rooms.domain.model.queries.QuoteRoomTypeQuery;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.UUID;
@Component public class AvailabilityContextFacadeImpl implements AvailabilityContextFacade {
    private final AvailabilityQueryService availability;
    public AvailabilityContextFacadeImpl(AvailabilityQueryService availability) { this.availability=availability; }
    public int countAvailable(UUID id,LocalDate checkIn,LocalDate checkOut) { return availability.countAvailable(new QuoteRoomTypeQuery(id,checkIn,checkOut)); }
    public RoomAvailabilitySnapshot quote(UUID id,LocalDate checkIn,LocalDate checkOut) { var quote=availability.quote(new QuoteRoomTypeQuery(id,checkIn,checkOut)); return new RoomAvailabilitySnapshot(quote.roomTypePublicId(),quote.checkIn(),quote.checkOut(),quote.nights(),quote.totalAmount(),quote.breakdown().stream().map(n->new RoomAvailabilitySnapshot.NightPrice(n.date(),n.price())).toList()); }
}
