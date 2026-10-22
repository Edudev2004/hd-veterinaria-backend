package com.vethd.backend.DTOs.request;

import lombok.Builder;

@Builder
public record ResetPasswordRequest(
        String token,
        String nuevaPassword,
        String password,
        String newPassword
) {
    public String getEffectiveToken() {
        if (token != null && !token.isBlank()) return token.trim();
        return null;
    }

    public String getEffectivePassword() {
        if (nuevaPassword != null && !nuevaPassword.isBlank()) return nuevaPassword;
        if (password != null && !password.isBlank()) return password;
        if (newPassword != null && !newPassword.isBlank()) return newPassword;
        return null;
    }
}
