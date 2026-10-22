package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.AppointmentCancelRequest;
import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentRescheduledRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/citas")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @PatchMapping("/citas/{id}/reprogramar")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRescheduledRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, request));
    }

    @PatchMapping("/citas/{id}/cancelar")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentCancelRequest request) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, request));
    }

    // US-17: Endpoints mis citas y detalle de cita
    @GetMapping("/citas")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID mascotaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(estado, mascotaId, fecha, busqueda));
    }

    @GetMapping("/citas/mis-citas")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointmentsAlias(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID mascotaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(estado, mascotaId, fecha, busqueda));
    }

    @GetMapping("/citas/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }
}
