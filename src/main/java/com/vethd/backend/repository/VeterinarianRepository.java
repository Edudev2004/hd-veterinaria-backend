package com.vethd.backend.repository;

import com.vethd.backend.model.Veterinarian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
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

    long countByActiveTrue();
    @Query(value = """
            SELECT DISTINCT v FROM Veterinarian v
            JOIN FETCH v.user u
            LEFT JOIN FETCH v.specialties
            WHERE (:nombre IS NULL OR 
                   LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:active IS NULL OR v.active = :active)
            """,
            countQuery = """
            SELECT COUNT(v) FROM Veterinarian v
            JOIN v.user u
            WHERE (:nombre IS NULL OR 
                   LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:active IS NULL OR v.active = :active)
            """)
    Page<Veterinarian> listarConFiltros(
            @Param("nombre") String nombre,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
            SELECT v FROM Veterinarian v
            JOIN FETCH v.user u
            LEFT JOIN FETCH v.specialties
            WHERE v.id = :id
            """)
    Optional<Veterinarian> findByIdWithDetails(@Param("id") UUID id);
}
