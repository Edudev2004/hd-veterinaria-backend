package com.vethd.backend.repository;

import com.vethd.backend.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {

    // US-22: para el chequeo 409 - evitar duplicar diagnostico en la misma cita
    boolean existsByAppointment_Id(UUID appointmentId);

    @Query("""
            SELECT d FROM Diagnosis d
            JOIN FETCH d.appointment a
            JOIN FETCH a.pet p
            JOIN FETCH p.owner o
            JOIN FETCH o.user ou
            JOIN FETCH a.veterinarian v
            JOIN FETCH v.user vu
            LEFT JOIN FETCH v.specialties
            WHERE p.id = :petId
            ORDER BY a.scheduledAt DESC
            """)
    List<Diagnosis> findByPetIdOrderByScheduledAtDesc(@Param("petId") UUID petId);
}
