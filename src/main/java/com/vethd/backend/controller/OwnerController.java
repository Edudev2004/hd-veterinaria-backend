package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.OwnerRequest;
import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/propietarios")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final OwnerRepository ownerRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/me")
    public OwnerResponse getProfile() {
        return ownerService.getProfile(resolveCurrentOwnerId());
    }

    @PutMapping("/me")
    public OwnerResponse updateProfile(@Valid @RequestBody OwnerRequest request) {
        return ownerService.updateProfile(resolveCurrentOwnerId(), request);
    }

    private UUID resolveCurrentOwnerId() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return ownerRepository.findByUserId(currentUserId)
                .or(() -> ownerRepository.findById(currentUserId))
                .map(Owner::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de propietario no encontrado para el usuario autenticado"));
    }
}
