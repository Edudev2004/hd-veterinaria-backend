package com.vethd.backend.repository;

import com.vethd.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vethd.backend.repository.projection.EstadoCitaCount;
import com.vethd.backend.repository.projection.VeterinarioCitasCount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
        @Query("SELECT a.status AS status, COUNT(a) AS count " +
       "FROM Appointment a " +
       "WHERE a.scheduledAt BETWEEN :start AND :end " +
       "GROUP BY a.status")
List<EstadoCitaCount> countAppointmentsByStatusBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
);

@Query("SELECT COUNT(a) FROM Appointment a WHERE a.scheduledAt BETWEEN :start AND :end")
long countAppointmentsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
);

@Query("SELECT a.veterinarian.id AS veterinarianId, " +
       "CONCAT(a.veterinarian.user.firstName, ' ', a.veterinarian.user.lastName) AS fullName, " +
       "COUNT(a) AS count " +
       "FROM Appointment a " +
       "WHERE a.status = 'COMPLETADA' " +
       "GROUP BY a.veterinarian.id, a.veterinarian.user.firstName, a.veterinarian.user.lastName " +
       "ORDER BY COUNT(a) DESC")
List<VeterinarioCitasCount> findRankingVeterinariosAtendidos();
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
}
