package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.ForgotPasswordRequest;
import com.vethd.backend.DTOs.request.LoginRequest;
import com.vethd.backend.DTOs.request.RegisterRequest;
import com.vethd.backend.DTOs.request.ResetPasswordRequest;
import com.vethd.backend.DTOs.response.AuthResponse;

public interface AuthService {
    AuthResponse registerOwner(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token);
    String requestPasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
