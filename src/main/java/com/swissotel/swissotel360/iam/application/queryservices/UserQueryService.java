package com.swissotel.swissotel360.iam.application.queryservices;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByPublicIdQuery;

import java.util.Optional;

public interface UserQueryService {
	Optional<User> handle(GetUserByIdQuery query);

	Optional<User> handle(GetUserByEmailQuery query);

	Optional<User> handle(GetUserByPublicIdQuery query);

}
