package com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.model;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.ArrayList;

@Getter
@EqualsAndHashCode
public class UserDetailsImpl implements UserDetails {
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().getStringName()));
        // These capabilities match the grants seeded in V1__init_schema.sql.
        switch (user.getRole().getStringName()) {
            case "ROLE_SUPER_ADMIN", "ROLE_MANAGER", "ROLE_RECEPTIONIST" -> {
                authorities.add(new SimpleGrantedAuthority("guests.view"));
                authorities.add(new SimpleGrantedAuthority("guests.manage"));
            }
            default -> { }
        }
        return new UserDetailsImpl(user.getEmail(), user.getPassword(), authorities);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
