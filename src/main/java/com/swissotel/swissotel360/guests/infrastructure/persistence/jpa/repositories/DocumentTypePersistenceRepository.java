package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities.DocumentTypePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypePersistenceRepository extends JpaRepository<DocumentTypePersistenceEntity, Long> { }
