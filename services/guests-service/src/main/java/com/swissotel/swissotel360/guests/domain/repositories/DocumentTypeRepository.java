package com.swissotel.swissotel360.guests.domain.repositories;

import com.swissotel.swissotel360.guests.domain.model.valueobjects.DocumentType;
import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository {
	Optional<DocumentType> findById(Long id);

	Optional<DocumentType> findByCode(String code);

	List<DocumentType> findAll();
}
