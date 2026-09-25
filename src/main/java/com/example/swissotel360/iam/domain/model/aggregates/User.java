package com.example.swissotel360.iam.domain.model.aggregates;

import com.example.swissotel360.iam.domain.model.entities.Role;
import com.example.swissotel360.iam.domain.model.valueobjects.EmailAddress;
import com.example.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * IAM User Domain Aggregate Root.
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {
    private Long id;
    private UUID publicId;
    private String fullName;
    private EmailAddress email;
    private String password;
    private Role role;
    private String status;
    private Instant lastLoginAt;

    public User() {
        this.publicId = UUID.randomUUID();
        this.status = "active";
    }

    public User(String fullName, String email, String password, Role role) {
        this(fullName, new EmailAddress(email), password, role);
    }

    public User(String fullName, EmailAddress email, String password, Role role) {
        this();
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.fullName = fullName.trim();
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Constructor used by Persistence Assembler
    public User(Long id, UUID publicId, String fullName, String email, String password, Role role, String status,
            Instant lastLoginAt) {
        this(id, publicId, fullName, new EmailAddress(email), password, role, status, lastLoginAt);
    }

    public User(Long id, UUID publicId, String fullName, EmailAddress email, String password, Role role, String status,
            Instant lastLoginAt) {
        this.id = id;
        this.publicId = publicId != null ? publicId : UUID.randomUUID();
        this.fullName = fullName != null ? fullName.trim() : "";
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status != null ? status : "active";
        this.lastLoginAt = lastLoginAt;
    }

    public String getEmail() {
        return email != null ? email.address() : null;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = Instant.now();
    }
}
