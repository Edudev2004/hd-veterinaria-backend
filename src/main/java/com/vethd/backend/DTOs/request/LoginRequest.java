package com.vethd.backend.DTOs.request;

import lombok.Builder;

@Builder
public record LoginRequest(
        String email,
        String correo,
        String password,
        String contrasena
) {
    public String getEffectiveEmail() {
        if (email != null && !email.isBlank()) return email.trim();
        if (correo != null && !correo.isBlank()) return correo.trim();
        return null;
    }

    public String getEffectivePassword() {
        if (password != null && !password.isBlank()) return password;
        if (contrasena != null && !contrasena.isBlank()) return contrasena;
        return null;
    }
}
