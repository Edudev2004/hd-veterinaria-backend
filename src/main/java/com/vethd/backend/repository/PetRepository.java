package com.vethd.backend.repository;

import com.vethd.backend.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {
    List<Pet> findByOwner_IdAndActiveTrueOrderByCreatedAtDesc(UUID ownerId);
}
