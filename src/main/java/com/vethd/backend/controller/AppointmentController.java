package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentRescheduledRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
