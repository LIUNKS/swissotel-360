package com.swissotel.swissotel360.rooms.interfaces.rest.transform;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.UpdateRoomTypeCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRoomTypeCommand;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.CreateRoomTypeResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.UpdateRoomTypeResource;
import java.util.UUID;
public final class RoomTypeCommandAssembler {
    private RoomTypeCommandAssembler() { }
    public static CreateRoomTypeCommand toCommand(CreateRoomTypeResource resource) { return new CreateRoomTypeCommand(resource.name(),resource.capacity(),resource.description()); }
    public static UpdateRoomTypeCommand toCommand(UUID publicId,UpdateRoomTypeResource resource) { return new UpdateRoomTypeCommand(publicId,resource.name(),resource.capacity(),resource.description()); }
    public static DeleteRoomTypeCommand toDeleteCommand(UUID publicId) { return new DeleteRoomTypeCommand(publicId); }
}
