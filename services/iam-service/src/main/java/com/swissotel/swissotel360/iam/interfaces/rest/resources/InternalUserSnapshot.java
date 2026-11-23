package com.swissotel.swissotel360.iam.interfaces.rest.resources;

import java.util.UUID;

/** Internal IAM user projection exposed strictly on /internal/users/** endpoints. */
public record InternalUserSnapshot(
		Long id,
		UUID publicId,
		String email,
		String fullName,
		String role,
		String status) {
}
