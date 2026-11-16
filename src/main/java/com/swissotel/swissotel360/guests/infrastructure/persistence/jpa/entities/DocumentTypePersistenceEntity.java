package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_types")
@Getter
@Setter
@NoArgsConstructor
public class DocumentTypePersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 20)
	private String code;
	@Column(nullable = false, length = 60)
	private String name;
	@Column(name = "has_check_digit", nullable = false)
	private boolean hasCheckDigit;
}
