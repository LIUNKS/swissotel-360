package com.swissotel.swissotel360.iam.application.internal.commandservices;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.hashing.HashingService;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.commands.SignInCommand;
import com.swissotel.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.EmailAddress;
import com.swissotel.swissotel360.iam.domain.repositories.RolePermissionRepository;
import com.swissotel.swissotel360.iam.domain.repositories.RoleRepository;
import com.swissotel.swissotel360.iam.domain.repositories.UserRepository;
import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

@Service
public class UserCommandServiceImpl implements UserCommandService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final HashingService hashingService;
	private final TokenService tokenService;

	public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			RolePermissionRepository rolePermissionRepository, HashingService hashingService, TokenService tokenService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.hashingService = hashingService;
		this.tokenService = tokenService;
	}

	@Override
	public Result<User, ApplicationError> handle(SignUpCommand command) {
		if (userRepository.existsByEmail(command.email())) {
			return Result.failure(ApplicationError.conflict("user", "Email already exists: " + command.email()));
		}
		var roleOpt = roleRepository.findByName(command.role());
		if (roleOpt.isEmpty()) {
			return Result.failure(ApplicationError.notFound("role", command.role().name()));
		}
		var role = roleOpt.get();
		try {
			var user = new User(command.fullName(), new EmailAddress(command.email()),
					hashingService.encode(command.password()), role);
			var savedUser = userRepository.save(user);
			return Result.success(savedUser);
		} catch (IllegalArgumentException e) {
			return Result.failure(ApplicationError.validationError("user", e.getMessage()));
		}
	}

	@Override
	public Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command) {
		var userOpt = userRepository.findByEmail(command.email());
		if (userOpt.isEmpty() || !hashingService.matches(command.password(), userOpt.get().getPassword())) {
			return Result.failure(ApplicationError.unauthorized("Invalid email or password"));
		}
		var user = userOpt.get();
		user.updateLastLoginAt();
		var updatedUser = userRepository.save(user);
		var permissions = rolePermissionRepository.findPermissionsByRoleId(updatedUser.getRole().getId());
		var token = tokenService.generateToken(updatedUser.getId(), updatedUser.getPublicId(), updatedUser.getEmail(),
				updatedUser.getRole().getStringName(), permissions);
		return Result.success(new ImmutablePair<>(updatedUser, token));
	}
}
