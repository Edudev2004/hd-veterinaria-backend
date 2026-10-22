
package com.vethd.backend.repository;

import com.vethd.backend.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository
        extends JpaRepository<Specialty, UUID> {

    List<Specialty> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    Optional<Specialty> findByNameIgnoreCase(String name);
}
