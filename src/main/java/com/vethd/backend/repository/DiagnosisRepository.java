package com.vethd.backend.repository;

import com.vethd.backend.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {

    // US-22: para el chequeo 409 - evitar duplicar diagnostico en la misma cita
    boolean existsByAppointment_Id(UUID appointmentId);

    Optional<Diagnosis> findByAppointment_Id(UUID appointmentId);
}
