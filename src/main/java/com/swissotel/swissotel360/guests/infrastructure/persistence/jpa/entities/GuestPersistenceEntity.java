package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "guests", uniqueConstraints = @UniqueConstraint(name = "uq_guests_document", columnNames = {"document_type_id", "document_number"}))
@Getter @Setter @NoArgsConstructor
public class GuestPersistenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    @Column(name = "user_id", unique = true)
    private Long userId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentTypePersistenceEntity documentType;
    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(length = 150)
    private String email;
    @Column(length = 30)
    private String phone;
    @Column(name = "country_of_origin", length = 60)
    private String countryOfOrigin;
    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt;
}
