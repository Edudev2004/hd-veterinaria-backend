
package com.vethd.backend.repository;

import com.vethd.backend.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository
        extends JpaRepository<Specialty, UUID> {

    // Listar especialidades activas
    List<Specialty> findByActiveTrue();

    // US-29: Validar duplicados al crear
    boolean existsByNameIgnoreCase(String name);

    // US-29: Validar duplicados al editar
    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    // develop: Buscar especialidad por nombre
    Optional<Specialty> findByNameIgnoreCase(String name);
}
