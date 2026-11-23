package com.swissotel.swissotel360.iam.domain.model.entities;

import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * Role domain entity.
 */
@Getter
@Setter
public class Role {
	private Long id;
	private Roles name;

	public Role() {
	}

	public Role(Roles name) {
		this.name = name;
	}

	public Role(Long id, Roles name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Get the name of the role as a string.
	 *
	 * @return the name of the role as a string
	 */
	public String getStringName() {
		return name != null ? name.name() : "";
	}

	/**
	 * Get the default role.
	 *
	 * @return the default role
	 */
	public static Role getDefaultRole() {
		return new Role(Roles.ROLE_GUEST);
	}

	/**
	 * Get the role from its name.
	 *
	 * @param name
	 *                 the name of the role
	 * @return the role
	 */
	public static Role toRoleFromName(String name) {
		return new Role(Roles.valueOf(name));
	}

	/**
	 * Validate the role set.
	 *
	 * @param roles
	 *                  the role set
	 * @return the role set
	 */
	public static List<Role> validateRoleSet(List<Role> roles) {
		if (roles == null || roles.isEmpty()) {
			return List.of(getDefaultRole());
		}
		return roles;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Role role))
			return false;
		return name == role.name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return getStringName();
	}
}
