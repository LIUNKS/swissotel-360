package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.application.commandservices.GuestCommandService;
import com.swissotel.swissotel360.guests.application.queryservices.GuestQueryService;
import com.swissotel.swissotel360.guests.domain.model.queries.FindGuestByDocumentQuery;
import com.swissotel.swissotel360.guests.domain.model.queries.GetGuestByPublicIdQuery;
import com.swissotel.swissotel360.guests.domain.model.queries.SearchGuestsQuery;
import com.swissotel.swissotel360.guests.interfaces.rest.resources.CreateGuestResource;
import com.swissotel.swissotel360.guests.interfaces.rest.resources.GuestResource;
import com.swissotel.swissotel360.guests.interfaces.rest.resources.UpdateGuestResource;
import com.swissotel.swissotel360.guests.interfaces.rest.transform.GuestCommandAssembler;
import com.swissotel.swissotel360.guests.interfaces.rest.transform.CreateGuestCommandAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guests")
@Tag(name = "Guests", description = "Guest identity and profile management")
public class GuestController {
    private final GuestCommandService commandService;
    private final GuestQueryService queryService;

    public GuestController(GuestCommandService commandService, GuestQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('guests.view')")
    @Operation(summary = "Search guests", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Paged guest results"), @ApiResponse(responseCode = "403", description = "Permission denied")})
    public ResponseEntity<?> search(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var query = new SearchGuestsQuery(q, page, size);
        var result = queryService.search(query.query(), query.page(), query.size());
        var content = result.content().stream().map(GuestResource::from).toList();
        return ResponseEntity.ok(new PageImpl<>(content, PageRequest.of(page, size), result.totalElements()));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('guests.view')")
    @Operation(summary = "Find guest by identity document", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Guest found", content = @Content(schema = @Schema(implementation = GuestResource.class))), @ApiResponse(responseCode = "404", description = "Guest not found"), @ApiResponse(responseCode = "403", description = "Permission denied")})
    public ResponseEntity<GuestResource> lookup(@RequestParam Long documentTypeId, @RequestParam String documentNumber) {
        var query = new FindGuestByDocumentQuery(documentTypeId, documentNumber);
        return ResponseEntity.ok(GuestResource.from(queryService.findByDocument(query.documentTypeId(), query.documentNumber())));
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAuthority('guests.view')")
    @Operation(summary = "Get guest by public identifier", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Guest found", content = @Content(schema = @Schema(implementation = GuestResource.class))), @ApiResponse(responseCode = "404", description = "Guest not found"), @ApiResponse(responseCode = "403", description = "Permission denied")})
    public ResponseEntity<GuestResource> getOne(@PathVariable UUID publicId) {
        return ResponseEntity.ok(GuestResource.from(queryService.getByPublicId(new GetGuestByPublicIdQuery(publicId).publicId())));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('guests.manage')")
    @Operation(summary = "Create guest", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Guest created", content = @Content(schema = @Schema(implementation = GuestResource.class))), @ApiResponse(responseCode = "400", description = "Invalid input"), @ApiResponse(responseCode = "404", description = "Document type not found"), @ApiResponse(responseCode = "409", description = "Duplicate document"), @ApiResponse(responseCode = "403", description = "Permission denied")})
    public ResponseEntity<GuestResource> create(@Valid @RequestBody CreateGuestResource resource) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GuestResource.from(commandService.create(CreateGuestCommandAssembler.toCommand(resource))));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAuthority('guests.manage')")
    @Operation(summary = "Update guest profile", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Guest updated", content = @Content(schema = @Schema(implementation = GuestResource.class))), @ApiResponse(responseCode = "400", description = "Invalid input"), @ApiResponse(responseCode = "404", description = "Guest not found"), @ApiResponse(responseCode = "403", description = "Permission denied")})
    public ResponseEntity<GuestResource> update(@PathVariable UUID publicId, @Valid @RequestBody UpdateGuestResource resource) {
        return ResponseEntity.ok(GuestResource.from(commandService.update(GuestCommandAssembler.toCommand(publicId, resource))));
    }
}
