package com.vethd.backend.repository;

import com.vethd.backend.model.Pet;
import com.vethd.backend.repository.projection.EspecieCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {

    // Solo mascotas activas (las de borrado lógico no cuentan) y sin distinguir mayúsculas ("Perro" = "perro").
    @Query("SELECT LOWER(p.species) AS species, COUNT(p) AS count FROM Pet p " +
           "WHERE p.active = true GROUP BY LOWER(p.species)")
    List<EspecieCount> countPetsBySpecies();

    long countByActiveTrue();

    List<Pet> findByOwner_IdAndActiveTrueOrderByCreatedAtDesc(UUID ownerId);
}
