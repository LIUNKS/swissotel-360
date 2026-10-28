package com.swissotel.swissotel360.rooms.interfaces.rest.transform;
import com.swissotel.swissotel360.rooms.application.queryservices.*;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.*;
public final class RoomsResourceAssembler {
    private RoomsResourceAssembler() { }
    public static RoomTypeResource toResource(RoomTypeView view) { var type=view.roomType(); return new RoomTypeResource(type.publicId(),type.name(),type.capacity(),type.description(),view.currentPrice()); }
    public static RoomTypeResource toResource(RoomType type) { return new RoomTypeResource(type.publicId(),type.name(),type.capacity(),type.description(),null); }
    public static RoomResource toResource(RoomView view) { var room=view.room(); return new RoomResource(room.publicId(),room.roomNumber(),room.floor(),view.roomTypePublicId(),view.roomTypeName(),room.status()); }
    public static RateResource toResource(Rate rate) { return new RateResource(rate.id(),rate.roomTypePublicId(),rate.price(),rate.season(),rate.validFrom(),rate.validTo()); }
    public static AvailabilityResource toResource(AvailabilityResult result) { return new AvailabilityResource(result.roomTypePublicId(),result.roomTypeName(),result.capacity(),result.availableRooms(),result.pricePerNight(),result.totalAmount()); }
    public static PriceQuoteResource toResource(PriceQuote quote) { return new PriceQuoteResource(quote.roomTypePublicId(),quote.checkIn(),quote.checkOut(),quote.nights(),quote.totalAmount(),quote.breakdown().stream().map(n->new NightPriceResource(n.date(),n.price())).toList()); }
}
