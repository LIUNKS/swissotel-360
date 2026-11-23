package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByPublicIdQuery;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.InternalUserSnapshot;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Internal service-to-service endpoint exposed for downstream microservices (blocked at API Gateway). */
@Hidden
@RestController
@RequestMapping("/internal/users")
public class UserInternalController {

	private final UserQueryService users;

	public UserInternalController(UserQueryService users) {
		this.users = users;
	}

	@GetMapping("/{userId}")
	public ResponseEntity<InternalUserSnapshot> getById(@PathVariable Long userId) {
		return users.handle(new GetUserByIdQuery(userId))
				.map(UserInternalController::toSnapshot)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-public-id/{userPublicId}")
	public ResponseEntity<InternalUserSnapshot> getByPublicId(@PathVariable UUID userPublicId) {
		return users.handle(new GetUserByPublicIdQuery(userPublicId))
				.map(UserInternalController::toSnapshot)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private static InternalUserSnapshot toSnapshot(User user) {
		return new InternalUserSnapshot(
				user.getId(),
				user.getPublicId(),
				user.getEmail(),
				user.getFullName(),
				user.getRole() != null ? user.getRole().getStringName() : null,
				user.getStatus());
	}
}
