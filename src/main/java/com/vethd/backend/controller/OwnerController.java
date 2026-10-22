package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.OwnerRequest;
import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.services.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/propietarios")
@RequiredArgsConstructor
public class OwnerController {

    private static final UUID PROPIETARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final OwnerService ownerService;

    @GetMapping("/me")
    public OwnerResponse getProfile() {
        return ownerService.getProfile(PROPIETARIO_DEMO_ID);
    }

    @PutMapping("/me")
    public OwnerResponse updateProfile(@Valid @RequestBody OwnerRequest request) {
        return ownerService.updateProfile(PROPIETARIO_DEMO_ID, request);
    }
}
