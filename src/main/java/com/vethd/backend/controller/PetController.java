package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.PetRequest;
import com.vethd.backend.DTOs.response.PetResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final OwnerRepository ownerRepository;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse register(@Valid @RequestBody PetRequest request) {
        return petService.register(resolveCurrentOwnerId(), request);
    }

    @GetMapping
    public List<PetResponse> list() {
        return petService.listByOwner(resolveCurrentOwnerId());
    }

    @PutMapping("/{id}")
    public PetResponse update(@PathVariable UUID id, @Valid @RequestBody PetRequest request) {
        return petService.update(resolveCurrentOwnerId(), id, request);
    }

    private UUID resolveCurrentOwnerId() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return ownerRepository.findByUserId(currentUserId)
                .or(() -> ownerRepository.findById(currentUserId))
                .map(Owner::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de propietario no encontrado para el usuario autenticado"));
    }
}
