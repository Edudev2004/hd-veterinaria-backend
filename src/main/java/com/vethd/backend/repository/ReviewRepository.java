package com.vethd.backend.repository;

import com.vethd.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 1. Valida si ya existe una reseña para esa cita (para el conflicto HTTP 409)
    boolean existsByAppointmentId(UUID appointmentId);

    // 2. Obtiene la lista de reseñas de un veterinario específico ordenadas por fecha
    @Query("SELECT r FROM Review r JOIN r.appointment a WHERE a.veterinarian.id = :veterinarianId ORDER BY r.createdAt DESC")
    List<Review> findByVeterinarianId(@Param("veterinarianId") UUID veterinarianId);

    // 3. Calcula el promedio de puntajes de las reseñas de un veterinario
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.appointment a WHERE a.veterinarian.id = :veterinarianId")
    Double findAverageRatingByVeterinarianId(@Param("veterinarianId") UUID veterinarianId);
}