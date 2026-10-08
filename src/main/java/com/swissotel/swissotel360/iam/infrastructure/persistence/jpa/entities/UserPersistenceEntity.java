package com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.entities;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RolePersistenceEntity role;
}
