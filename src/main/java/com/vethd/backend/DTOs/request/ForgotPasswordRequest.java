package com.vethd.backend.DTOs.request;

import lombok.Builder;

@Builder
public record ForgotPasswordRequest(
        String correo,
        String email
) {
    public String getEffectiveEmail() {
        if (correo != null && !correo.isBlank()) return correo.trim();
        if (email != null && !email.isBlank()) return email.trim();
        return null;
    }
}
