package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.adapters;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.assemblers.RoomsPersistenceAssembler;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomTypePersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;
import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomTypeException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeInUseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository public class RoomTypeRepositoryImpl implements RoomTypeRepository {
    private final RoomTypePersistenceRepository repository;
    public RoomTypeRepositoryImpl(RoomTypePersistenceRepository repository) { this.repository=repository; }
    public RoomType save(RoomType type) { try { return RoomsPersistenceAssembler.toDomain(repository.saveAndFlush(RoomsPersistenceAssembler.toEntity(type))); } catch(DataIntegrityViolationException ex) { throw new DuplicateRoomTypeException(type.name()); } }
    public Optional<RoomType> findByPublicId(UUID id) { return repository.findByPublicId(id).map(RoomsPersistenceAssembler::toDomain); }
    public Optional<RoomType> findById(Long id) { return repository.findById(id).map(RoomsPersistenceAssembler::toDomain); }
    public List<RoomType> findAll() { return repository.findAll().stream().map(RoomsPersistenceAssembler::toDomain).toList(); }
    public boolean existsByNameIgnoreCase(String name) { return repository.existsByNameIgnoreCase(name); }
    public boolean existsByNameIgnoreCaseAndIdNot(String name,Long id) { return repository.existsByNameIgnoreCaseAndIdNot(name,id); }
    public boolean existsById(Long id) { return repository.existsById(id); }
    public void delete(RoomType type) { try { repository.deleteById(type.id()); repository.flush(); } catch(DataIntegrityViolationException ex) { throw new RoomTypeInUseException(type.publicId()); } }
}
