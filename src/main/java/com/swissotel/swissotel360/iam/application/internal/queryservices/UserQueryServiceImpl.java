package com.swissotel.swissotel360.iam.application.internal.queryservices;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;

import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByPublicIdQuery;
import com.swissotel.swissotel360.iam.domain.repositories.UserRepository;
import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }

    @Override
    public Optional<User> handle(GetUserByPublicIdQuery query) {
        return userRepository.findByPublicId(query.publicId());
    }
}
