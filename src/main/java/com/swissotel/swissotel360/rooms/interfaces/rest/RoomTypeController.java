package com.swissotel.swissotel360.rooms.interfaces.rest;
import com.swissotel.swissotel360.rooms.application.commandservices.RoomTypeCommandService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomTypeQueryService;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomTypeByPublicIdQuery;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.*;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RoomTypeCommandAssembler;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RoomsResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/room-types") @Tag(name="Room types",description="Room type catalogue and management")
public class RoomTypeController {
    private final RoomTypeCommandService commands; private final RoomTypeQueryService queries;
    public RoomTypeController(RoomTypeCommandService commands,RoomTypeQueryService queries) { this.commands=commands; this.queries=queries; }
    @GetMapping @PreAuthorize("hasAuthority('rooms.view')") @Operation(summary="List room types",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="200",description="Room types with current prices"),@ApiResponse(responseCode="403",description="Permission denied")})
    public ResponseEntity<?> list() { return ResponseEntity.ok(queries.findAll().stream().map(RoomsResourceAssembler::toResource).toList()); }
    @GetMapping("/{publicId}") @PreAuthorize("hasAuthority('rooms.view')") @Operation(summary="Get a room type",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="200",description="Room type found"),@ApiResponse(responseCode="404",description="Room type not found")})
    public ResponseEntity<RoomTypeResource> get(@PathVariable UUID publicId) { return ResponseEntity.ok(RoomsResourceAssembler.toResource(queries.findByPublicId(new GetRoomTypeByPublicIdQuery(publicId)))); }
    @PostMapping @PreAuthorize("hasAuthority('rooms.manage')") @Operation(summary="Create a room type",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="201",description="Room type created"),@ApiResponse(responseCode="400",description="Invalid request"),@ApiResponse(responseCode="409",description="Duplicate room type")})
    public ResponseEntity<RoomTypeResource> create(@Valid @RequestBody CreateRoomTypeResource r) { var created=commands.create(RoomTypeCommandAssembler.toCommand(r)); return ResponseEntity.status(HttpStatus.CREATED).body(RoomsResourceAssembler.toResource(created)); }
    @PutMapping("/{publicId}") @PreAuthorize("hasAuthority('rooms.manage')") @Operation(summary="Update a room type",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="200",description="Room type updated"),@ApiResponse(responseCode="404",description="Room type not found"),@ApiResponse(responseCode="409",description="Duplicate room type")})
    public ResponseEntity<RoomTypeResource> update(@PathVariable UUID publicId,@Valid @RequestBody UpdateRoomTypeResource r) { commands.update(RoomTypeCommandAssembler.toCommand(publicId,r)); return ResponseEntity.ok(RoomsResourceAssembler.toResource(queries.findByPublicId(new GetRoomTypeByPublicIdQuery(publicId)))); }
    @DeleteMapping("/{publicId}") @PreAuthorize("hasAuthority('rooms.manage')") @Operation(summary="Delete an unused room type",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="204",description="Room type deleted"),@ApiResponse(responseCode="404",description="Room type not found"),@ApiResponse(responseCode="409",description="Room type is in use")})
    public ResponseEntity<Void> delete(@PathVariable UUID publicId) { commands.delete(RoomTypeCommandAssembler.toDeleteCommand(publicId)); return ResponseEntity.noContent().build(); }
}
