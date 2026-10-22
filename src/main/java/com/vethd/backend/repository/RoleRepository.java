
package com.vethd.backend.repository;

import com.vethd.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Buscar rol por nombre exacto
    Optional<Role> findByName(String name);

    // Buscar rol sin distinguir mayusculas y minusculas
    Optional<Role> findByNameIgnoreCase(String name);

    // Validar si ya existe un rol con ese nombre
    boolean existsByNameIgnoreCase(String name);

    // Validar nombres duplicados al editar
    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );
}
