package com.vethd.backend.repository;

import com.vethd.backend.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
    List<Specialty> findByActiveTrue();
}
