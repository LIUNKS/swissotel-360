package com.swissotel.swissotel360.iam.application.commandservices;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.commands.SignInCommand;
import com.swissotel.swissotel360.iam.domain.model.commands.SignUpCommand;
import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface UserCommandService {
	Result<User, ApplicationError> handle(SignUpCommand command);

	Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command);
}
