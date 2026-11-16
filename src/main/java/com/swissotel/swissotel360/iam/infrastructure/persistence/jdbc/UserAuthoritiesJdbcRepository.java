package com.swissotel.swissotel360.iam.infrastructure.persistence.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserAuthoritiesJdbcRepository {
	private final JdbcTemplate jdbcTemplate;

	public UserAuthoritiesJdbcRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<String> findPermissionsByRoleId(Long roleId) {
		return jdbcTemplate.queryForList(
				"select p.code from permissions p join role_permissions rp on rp.permission_id=p.id where rp.role_id=?",
				String.class, roleId);
	}
}
