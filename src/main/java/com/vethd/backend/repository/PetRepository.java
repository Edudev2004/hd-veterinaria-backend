package com.vethd.backend.repository;

import com.vethd.backend.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vethd.backend.repository.projection.EspecieCount;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {
    @Query("SELECT p.species AS species, COUNT(p) AS count FROM Pet p GROUP BY p.species")
List<EspecieCount> countPetsBySpecies();
}
