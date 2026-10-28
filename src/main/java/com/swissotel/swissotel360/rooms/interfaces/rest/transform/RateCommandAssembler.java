package com.swissotel.swissotel360.rooms.interfaces.rest.transform;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRateCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.CloseRateCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRateCommand;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.CreateRateResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.CloseRateResource;
import java.util.UUID;
public final class RateCommandAssembler {
    private RateCommandAssembler() { }
    public static CreateRateCommand toCommand(UUID roomTypePublicId,CreateRateResource resource) { return new CreateRateCommand(roomTypePublicId,resource.price(),resource.season(),resource.validFrom(),resource.validTo()); }
    public static CloseRateCommand toCommand(UUID roomTypePublicId,Long rateId,CloseRateResource resource) { return new CloseRateCommand(roomTypePublicId,rateId,resource.validTo()); }
    public static DeleteRateCommand toDeleteCommand(UUID roomTypePublicId,Long rateId) { return new DeleteRateCommand(roomTypePublicId,rateId); }
}
