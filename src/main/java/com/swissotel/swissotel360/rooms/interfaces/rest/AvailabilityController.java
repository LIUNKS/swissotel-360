package com.swissotel.swissotel360.rooms.interfaces.rest;
import com.swissotel.swissotel360.rooms.application.queryservices.AvailabilityQueryService;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.AvailabilityResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.PriceQuoteResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.AvailabilityQueryAssembler;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RoomsResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;
@RestController @RequestMapping("/api") @Tag(name="Room availability",description="Search availability and quote room rates")
public class AvailabilityController {
    private final AvailabilityQueryService availability;
    public AvailabilityController(AvailabilityQueryService availability) { this.availability=availability; }
    @GetMapping("/availability") @PreAuthorize("isAuthenticated()") @Operation(summary="Search room type availability",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="200",description="Availability and price by room type"),@ApiResponse(responseCode="400",description="Invalid date range"),@ApiResponse(responseCode="422",description="A rate does not cover a requested night")})
    public ResponseEntity<?> search(@RequestParam LocalDate checkIn,@RequestParam LocalDate checkOut,@RequestParam(required=false) @Positive Integer guests) { return ResponseEntity.ok(availability.search(AvailabilityQueryAssembler.toSearch(checkIn,checkOut,guests)).stream().map(RoomsResourceAssembler::toResource).toList()); }
    @GetMapping("/room-types/{publicId}/quote") @PreAuthorize("isAuthenticated()") @Operation(summary="Quote a room type night by night",security=@SecurityRequirement(name="bearerAuth")) @ApiResponses({@ApiResponse(responseCode="200",description="Price quote"),@ApiResponse(responseCode="400",description="Invalid date range"),@ApiResponse(responseCode="404",description="Room type not found"),@ApiResponse(responseCode="422",description="A rate does not cover a requested night")})
    public ResponseEntity<PriceQuoteResource> quote(@PathVariable UUID publicId,@RequestParam LocalDate checkIn,@RequestParam LocalDate checkOut) { return ResponseEntity.ok(RoomsResourceAssembler.toResource(availability.quote(AvailabilityQueryAssembler.toQuote(publicId,checkIn,checkOut)))); }
}
