package com.example.swissotel360.iam.application.commandservices;

import com.example.swissotel360.iam.domain.model.aggregates.User;
import com.example.swissotel360.iam.domain.model.commands.SignInCommand;
import com.example.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.example.swissotel360.iam.domain.model.commands.SignOutCommand;
import com.example.swissotel360.shared.application.result.ApplicationError;
import com.example.swissotel360.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Application service contract for IAM user commands.
 */
public interface UserCommandService {
    /**
     * Handles user sign-in.
     *
     * @param command sign-in command
     * @return authenticated user and token pair, or an application error
     */
    Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command);

    /**
     * Handles user sign-up.
     *
     * @param command sign-up command
     * @return created user aggregate, or an application error
     */
    Result<User, ApplicationError> handle(SignUpCommand command);
    /**
     * Handles user sign-out.
     *
     * @param command sign-out command
     * @return a result with no value, or an application error
     */
    Result<Void, ApplicationError> handle(SignOutCommand command);

}

