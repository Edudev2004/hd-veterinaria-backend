package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.RegisterRequest;
import com.vethd.backend.DTOs.response.AuthResponse;

public interface AuthService {
    AuthResponse registerOwner(RegisterRequest request);
}
