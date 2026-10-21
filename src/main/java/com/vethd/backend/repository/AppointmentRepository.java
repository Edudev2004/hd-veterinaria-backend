package com.vethd.backend.repository;

import com.vethd.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByVeterinarian_IdAndScheduledAtBetweenOrderByScheduledAtAsc(
            UUID veterinarianId,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
