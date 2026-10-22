
package com.vethd.backend.repository;

import com.vethd.backend.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpecialtyRepository
        extends JpaRepository<Specialty, UUID> {

    // Listar especialidades activas
    List<Specialty> findByActiveTrue();

    // Comprobar si existe una especialidad
    // con el mismo nombre
    boolean existsByNameIgnoreCase(String name);

    // Comprobar duplicados al editar,
    // excluyendo la misma especialidad
    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );
}
