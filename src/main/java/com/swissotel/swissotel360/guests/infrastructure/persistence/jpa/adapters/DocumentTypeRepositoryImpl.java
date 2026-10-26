package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.guests.domain.model.valueobjects.DocumentType;
import com.swissotel.swissotel360.guests.domain.repositories.DocumentTypeRepository;
import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.repositories.DocumentTypePersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class DocumentTypeRepositoryImpl implements DocumentTypeRepository {
    private final DocumentTypePersistenceRepository repository;
    public DocumentTypeRepositoryImpl(DocumentTypePersistenceRepository repository) { this.repository = repository; }

    @Override
    public Optional<DocumentType> findById(Long id) {
        return repository.findById(id).map(entity -> new DocumentType(entity.getId(), entity.getCode(), entity.getName(), entity.isHasCheckDigit()));
    }

    @Override
    public List<DocumentType> findAll() {
        return repository.findAll(SortByCode.INSTANCE).stream()
                .map(entity -> new DocumentType(entity.getId(), entity.getCode(), entity.getName(), entity.isHasCheckDigit())).toList();
    }

    private static final class SortByCode {
        private static final org.springframework.data.domain.Sort INSTANCE = org.springframework.data.domain.Sort.by("code").ascending();
    }
}
