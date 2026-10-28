package com.swissotel.swissotel360.iam.application.internal.commandservices;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.hashing.HashingService;
import com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.commands.SignInCommand;
import com.swissotel.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.EmailAddress;
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
	private final HashingService hashingService;
	private final TokenService tokenService;

	public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			HashingService hashingService, TokenService tokenService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
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
		if (userOpt.isEmpty()) {
			return Result.failure(ApplicationError.notFound("user", "User not found with email: " + command.email()));
		}
		var user = userOpt.get();
		if (!hashingService.matches(command.password(), user.getPassword())) {
			return Result.failure(ApplicationError.validationError("credentials", "Invalid credentials"));
		}
		user.updateLastLoginAt();
		var updatedUser = userRepository.save(user);
		var token = tokenService.generateToken(updatedUser.getEmail());
		return Result.success(new ImmutablePair<>(updatedUser, token));
	}
}
