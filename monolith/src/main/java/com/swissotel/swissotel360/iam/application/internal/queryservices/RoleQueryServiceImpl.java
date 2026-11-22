package com.swissotel.swissotel360.iam.application.internal.queryservices;

import com.swissotel.swissotel360.iam.application.queryservices.RoleQueryService;
import com.swissotel.swissotel360.iam.domain.model.entities.Role;
import com.swissotel.swissotel360.iam.domain.model.queries.GetRoleByNameQuery;
import com.swissotel.swissotel360.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that resolves IAM role read queries.
 */
@Service
public class RoleQueryServiceImpl implements RoleQueryService {
	private final RoleRepository roleRepository;

	public RoleQueryServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public Optional<Role> handle(GetRoleByNameQuery query) {
		return roleRepository.findByName(query.name());
	}
}
