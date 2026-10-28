package com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.services;

import com.swissotel.swissotel360.iam.domain.repositories.UserRepository;
import com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.swissotel.swissotel360.iam.infrastructure.persistence.jdbc.UserAuthoritiesJdbcRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserAuthoritiesJdbcRepository authoritiesRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, UserAuthoritiesJdbcRepository authoritiesRepository) {
        this.userRepository = userRepository;
        this.authoritiesRepository = authoritiesRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserDetailsImpl.build(user, authoritiesRepository.findPermissionsByRoleId(user.getRole().getId()));
    }
}
