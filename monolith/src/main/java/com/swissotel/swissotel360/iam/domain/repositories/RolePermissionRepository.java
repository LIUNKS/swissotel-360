package com.swissotel.swissotel360.iam.domain.repositories;

import java.util.List;

/**
 * Domain repository port for resolving permission codes granted to a role.
 */
public interface RolePermissionRepository {
	List<String> findPermissionsByRoleId(Long roleId);
}
