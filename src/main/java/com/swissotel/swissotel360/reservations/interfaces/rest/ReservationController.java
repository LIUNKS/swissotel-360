package com.swissotel.swissotel360.reservations.interfaces.rest;

import com.swissotel.swissotel360.reservations.application.command.*;
import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort;
import com.swissotel.swissotel360.reservations.application.query.*;
import com.swissotel.swissotel360.reservations.application.queryservices.ReservationQueryService;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
import com.swissotel.swissotel360.reservations.interfaces.rest.resources.*;
import com.swissotel.swissotel360.reservations.interfaces.rest.transform.ReservationRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@Tag(name="Reservations",description="Reservation lifecycle and guest bookings")
public class ReservationController {
    private final ReservationCommandService commands; private final ReservationQueryService queries; private final CurrentActorPort actors;
    public ReservationController(ReservationCommandService commands,ReservationQueryService queries,CurrentActorPort actors) { this.commands=commands; this.queries=queries; this.actors=actors; }

    @PostMapping @PreAuthorize("hasAuthority('reservations.create')")
    @Operation(summary="Create a reservation",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="201",description="Reservation created"),@ApiResponse(responseCode="400",description="Invalid stay"),@ApiResponse(responseCode="404",description="Guest or room type not found"),@ApiResponse(responseCode="409",description="No availability or duplicate reservation"),@ApiResponse(responseCode="422",description="Missing room rate")})
    public ResponseEntity<ReservationResource> create(@Valid @RequestBody CreateReservationResource resource) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationRestMapper.toResource(commands.create(ReservationRestMapper.toCommand(resource))));
    }

    @GetMapping @PreAuthorize("hasAuthority('reservations.view')")
    @Operation(summary="Search reservations",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="200",description="Paged reservations"),@ApiResponse(responseCode="400",description="Invalid filter")})
    public ResponseEntity<?> search(@RequestParam(required=false) String status,@RequestParam(required=false) UUID guestPublicId,
            @RequestParam(required=false) String code,@RequestParam(required=false) LocalDate checkInFrom,
            @RequestParam(required=false) LocalDate checkInTo,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        var parsedStatus=status==null?null:ReservationStatus.fromCode(status);
        var result=queries.search(new SearchReservationsQuery(parsedStatus,guestPublicId,code,checkInFrom,checkInTo,page,size),actors.current());
        return ResponseEntity.ok(new PageImpl<>(result.content().stream().map(ReservationRestMapper::toSummary).toList(),PageRequest.of(page,size),result.totalElements()));
    }

    @GetMapping("/by-code/{code}") @PreAuthorize("hasAuthority('reservations.view')")
    @Operation(summary="Get a reservation by code",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="200",description="Reservation found"),@ApiResponse(responseCode="404",description="Reservation not found")})
    public ResponseEntity<ReservationResource> getByCode(@PathVariable String code) { return ResponseEntity.ok(ReservationRestMapper.toResource(queries.findByCode(new GetReservationByCodeQuery(code),actors.current()))); }

    @GetMapping("/{publicId}") @PreAuthorize("hasAuthority('reservations.view')")
    @Operation(summary="Get a reservation",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="200",description="Reservation found"),@ApiResponse(responseCode="404",description="Reservation not found")})
    public ResponseEntity<ReservationResource> get(@PathVariable UUID publicId) { return ResponseEntity.ok(ReservationRestMapper.toResource(queries.findByPublicId(new GetReservationQuery(publicId),actors.current()))); }

    @PutMapping("/{publicId}") @PreAuthorize("hasAuthority('reservations.create')")
    @Operation(summary="Modify a reservation",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="200",description="Reservation updated"),@ApiResponse(responseCode="404",description="Reservation not found"),@ApiResponse(responseCode="409",description="Reservation cannot be modified or no availability")})
    public ResponseEntity<ReservationResource> modify(@PathVariable UUID publicId,@Valid @RequestBody ModifyReservationResource resource) {
        commands.modify(ReservationRestMapper.toCommand(publicId,resource));
        return ResponseEntity.ok(ReservationRestMapper.toResource(queries.findByPublicId(new GetReservationQuery(publicId),actors.current())));
    }

    @PostMapping("/{publicId}/confirm") @PreAuthorize("hasAuthority('payments.process')")
    @Operation(summary="Confirm a reservation after prepayment",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="200",description="Reservation confirmed"),@ApiResponse(responseCode="404",description="Reservation not found"),@ApiResponse(responseCode="409",description="Invalid status transition")})
    public ResponseEntity<ReservationResource> confirm(@PathVariable UUID publicId) {
        commands.confirm(new ConfirmReservationCommand(publicId));
        return ResponseEntity.ok(ReservationRestMapper.toResource(queries.findByPublicId(new GetReservationQuery(publicId),actors.current())));
    }

    @PostMapping("/{publicId}/cancel") @PreAuthorize("hasAuthority('reservations.cancel')")
    @Operation(summary="Cancel a reservation",security=@SecurityRequirement(name="bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode="200",description="Reservation cancelled"),@ApiResponse(responseCode="404",description="Reservation not found"),@ApiResponse(responseCode="409",description="Invalid status transition")})
    public ResponseEntity<ReservationResource> cancel(@PathVariable UUID publicId,@Valid @RequestBody(required=false) CancelReservationResource resource) {
        commands.cancel(new CancelReservationCommand(publicId,resource==null?null:resource.note()));
        return ResponseEntity.ok(ReservationRestMapper.toResource(queries.findByPublicId(new GetReservationQuery(publicId),actors.current())));
    }
}
