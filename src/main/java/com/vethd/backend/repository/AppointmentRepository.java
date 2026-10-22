package com.vethd.backend.repository;

import com.vethd.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByVeterinarian_IdAndScheduledAtBetweenOrderByScheduledAtAsc(
            UUID veterinarianId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Appointment> findByVeterinarian_IdAndScheduledAtBetweenAndStatus(
            UUID veterinarianId,
            LocalDateTime from,
            LocalDateTime to,
            String status
    );

    boolean existsByVeterinarian_IdAndScheduledAtAndStatus(
            UUID veterinarianId,
            LocalDateTime scheduledAt,
            String status
    );

    boolean existsByVeterinarian_IdAndScheduledAtAndStatusAndIdNot(
            UUID veterinarianId,
            LocalDateTime scheduledAt,
            String status,
            UUID id
    );

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            JOIN FETCH a.pet p
            JOIN FETCH p.owner o
            JOIN FETCH o.user ou
            JOIN FETCH a.veterinarian v
            JOIN FETCH v.user vu
            LEFT JOIN FETCH v.specialties
            JOIN FETCH a.specialty
            WHERE ou.id = :userId
            ORDER BY a.scheduledAt DESC
            """)
    List<Appointment> findByPetOwnerUserIdOrderByScheduledAtDesc(@Param("userId") UUID userId);

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            JOIN FETCH a.pet p
            JOIN FETCH p.owner o
            JOIN FETCH o.user ou
            JOIN FETCH a.veterinarian v
            JOIN FETCH v.user vu
            LEFT JOIN FETCH v.specialties
            JOIN FETCH a.specialty
            WHERE vu.id = :userId
            ORDER BY a.scheduledAt DESC
            """)
    List<Appointment> findByVeterinarianUserIdOrderByScheduledAtDesc(@Param("userId") UUID userId);

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            JOIN FETCH a.pet p
            JOIN FETCH p.owner o
            JOIN FETCH o.user ou
            JOIN FETCH a.veterinarian v
            JOIN FETCH v.user vu
            LEFT JOIN FETCH v.specialties
            JOIN FETCH a.specialty
            ORDER BY a.scheduledAt DESC
            """)
    List<Appointment> findAllWithDetailsOrderByScheduledAtDesc();

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            JOIN FETCH a.pet p
            JOIN FETCH p.owner o
            JOIN FETCH o.user ou
            JOIN FETCH a.veterinarian v
            JOIN FETCH v.user vu
            LEFT JOIN FETCH v.specialties
            JOIN FETCH a.specialty
            WHERE a.id = :id
            """)
    Optional<Appointment> findByIdWithDetails(@Param("id") UUID id);
}
