package com.swissotel.swissotel360.shared.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Stateless security principal reconstructed from a verified JWT without querying IAM persistence.
 */
public final class AuthenticatedUserPrincipal implements UserDetails {
	private final Long userId;
	private final UUID publicId;
	private final String email;
	private final String role;
	private final Set<String> permissions;
	private final Collection<? extends GrantedAuthority> authorities;

	public AuthenticatedUserPrincipal(Long userId, UUID publicId, String email, String role, Set<String> permissions) {
		if (userId == null || publicId == null || email == null || email.isBlank() || role == null || role.isBlank()
				|| permissions == null) {
			throw new IllegalArgumentException("Authenticated principal fields are required");
		}
		this.userId = userId;
		this.publicId = publicId;
		this.email = email;
		this.role = role;
		this.permissions = Set.copyOf(permissions);
		var granted = new ArrayList<GrantedAuthority>(1 + this.permissions.size());
		granted.add(new SimpleGrantedAuthority(role));
		this.permissions.stream().map(SimpleGrantedAuthority::new).forEach(granted::add);
		this.authorities = List.copyOf(granted);
	}

	public Long userId() {
		return userId;
	}

	public UUID publicId() {
		return publicId;
	}

	public String email() {
		return email;
	}

	public String role() {
		return role;
	}

	public Set<String> permissions() {
		return permissions;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return "";
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
