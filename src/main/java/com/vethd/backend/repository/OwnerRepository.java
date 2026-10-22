package com.vethd.backend.repository;

import com.vethd.backend.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    Optional<Owner> findByUserId(UUID userId);
    Optional<Owner> findByUser_Email(String email);
}
