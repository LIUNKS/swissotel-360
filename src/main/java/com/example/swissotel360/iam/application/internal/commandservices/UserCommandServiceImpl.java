package com.example.swissotel360.iam.application.internal.commandservices;

import com.example.swissotel360.iam.application.commandservices.UserCommandService;
import com.example.swissotel360.iam.application.internal.outboundservices.hashing.HashingService;
import com.example.swissotel360.iam.application.internal.outboundservices.tokens.TokenService;
import com.example.swissotel360.iam.domain.model.aggregates.User;
import com.example.swissotel360.iam.domain.model.commands.SignInCommand;
import com.example.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.example.swissotel360.iam.domain.model.commands.SignOutCommand;
import com.example.swissotel360.iam.domain.repositories.RoleRepository;
import com.example.swissotel360.iam.domain.repositories.UserRepository;
import com.example.swissotel360.shared.application.result.ApplicationError;
import com.example.swissotel360.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

/**
 * User command service implementation.
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
    }

    @Override
    public Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.username()));
        }
        if (!hashingService.matches(command.password(), user.get().getPassword())) {
            return Result.failure(ApplicationError.validationError("credentials", "Invalid username or password"));
        }
        var userModel = user.get();
        userModel.startNewSession();
        userRepository.save(userModel);
        
        var token = tokenService.generateToken(userModel.getUsername(), userModel.getActiveSessionId());
        return Result.success(ImmutablePair.of(userModel, token));
    }

    @Override
    public Result<User, ApplicationError> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            return Result.failure(ApplicationError.conflict("User", "Username already exists"));
        }
        var roles = command.roles().stream()
                .map(role -> roleRepository.findByName(role.getName()))
                .toList();

        if (roles.stream().anyMatch(java.util.Optional::isEmpty)) {
            return Result.failure(ApplicationError.notFound("Role", "one or more role names"));
        }

        var resolvedRoles = roles.stream()
                .map(java.util.Optional::get)
                .toList();

        var user = new User(command.username(), hashingService.encode(command.password()), resolvedRoles);
        userRepository.save(user);
        return userRepository.findByUsername(command.username())
                .<Result<User, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(ApplicationError.unexpected("sign-up", "Created user could not be reloaded")));
    }

    @Override
    public Result<Void, ApplicationError> handle(SignOutCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.username()));
        }
        var userModel = user.get();
        userModel.endSession();
        userRepository.save(userModel);
        return Result.success(null);
    }
}
