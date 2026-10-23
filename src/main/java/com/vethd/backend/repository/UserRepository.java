
package com.vethd.backend.repository;

import com.vethd.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    // Verificar si un rol esta asignado a algun usuario
    boolean existsByRole_Id(UUID roleId);

    // Contar usuarios que tienen asignado un rol
    long countByRole_Id(UUID roleId);
}
