package com.swissotel.swissotel360.iam.interfaces.acl;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link IamContextFacade}.
 */
@Service
public class IamContextFacadeImpl implements IamContextFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public IamContextFacadeImpl(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @Override
    public Long createUser(String fullName, String email, String password) {
        var signUpCommand = new SignUpCommand(fullName, email, password, Roles.ROLE_GUEST);
        var result = userCommandService.handle(signUpCommand);
        return result.toOptional().map(User::getId).orElse(0L);
    }

    @Override
    public Long fetchUserIdByEmail(String email) {
        var getUserByEmailQuery = new GetUserByEmailQuery(email);
        return userQueryService.handle(getUserByEmailQuery)
                .map(User::getId)
                .orElse(0L);
    }

    @Override
    public String fetchEmailByUserId(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        return userQueryService.handle(getUserByIdQuery)
                .map(User::getEmail)
                .orElse(StringUtils.EMPTY);
    }
}
