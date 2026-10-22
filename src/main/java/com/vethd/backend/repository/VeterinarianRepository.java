package com.vethd.backend.repository;

import com.vethd.backend.model.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeterinarianRepository extends JpaRepository<Veterinarian, UUID> {

    Optional<Veterinarian> findByUserId(UUID userId);

    @Query("""
            SELECT DISTINCT v FROM Veterinarian v
            JOIN FETCH v.user
            LEFT JOIN FETCH v.specialties
            WHERE v.active = true
            """)
    List<Veterinarian> findActiveAll();


    @Query("""
            SELECT DISTINCT v FROM Veterinarian v
            JOIN FETCH v.user
            LEFT JOIN FETCH v.specialties
            WHERE v.active = true
              AND EXISTS (SELECT s FROM v.specialties s WHERE s.id = :specialtyId)
            """)
    List<Veterinarian> findActiveBySpecialty(@Param("specialtyId") UUID specialtyId);

    @Query("""
            SELECT a.veterinarian.id, AVG(r.rating)
            FROM Review r JOIN r.appointment a
            GROUP BY a.veterinarian.id
            """)
    List<Object[]> findAverageRatings();
}
