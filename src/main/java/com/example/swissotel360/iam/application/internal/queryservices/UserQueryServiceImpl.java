package com.example.swissotel360.iam.application.internal.queryservices;

import com.example.swissotel360.iam.domain.model.aggregates.User;

import com.example.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.example.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.example.swissotel360.iam.domain.repositories.UserRepository;
import com.example.swissotel360.iam.application.queryservices.UserQueryService;
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
}
