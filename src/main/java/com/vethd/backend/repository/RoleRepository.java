
package com.vethd.backend.repository;

import com.vethd.backend.model.Role;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    Optional<Role> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    // Obtener los roles junto con sus permisos
    @Override
    @EntityGraph(attributePaths = {"permissions"})
    List<Role> findAll();

    // Obtener un rol junto con sus permisos
    @Override
    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findById(UUID id);
}
