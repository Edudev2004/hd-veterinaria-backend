package com.vethd.backend.DTOs.request;

import lombok.Builder;

@Builder
public record AppointmentNotAttendedRequest(
        String reason
) {
    
}