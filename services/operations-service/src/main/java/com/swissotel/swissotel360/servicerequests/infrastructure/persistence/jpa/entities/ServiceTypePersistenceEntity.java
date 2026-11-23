package com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "service_types")
@Getter
@Setter
public class ServiceTypePersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 30)
	private String code;
	@Column(nullable = false, length = 80)
	private String name;
	@Column(name = "operational_area", nullable = false, length = 60)
	private String operationalArea;
}
