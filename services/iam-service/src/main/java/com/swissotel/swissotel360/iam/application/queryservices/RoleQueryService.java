package com.swissotel.swissotel360.iam.application.queryservices;

import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.Optional;

/**
 * Application service contract for IAM role-read queries.
 */
public interface RoleQueryService {

	/**
	 * Handles retrieval of a role by name.
	 *
	 * @param query
	 *                  role-name query
	 * @return matching role, if found
	 */
	Optional<Role> handle(GetRoleByNameQuery query);
}
