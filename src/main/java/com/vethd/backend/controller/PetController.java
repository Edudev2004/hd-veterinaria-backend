package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.PetRequest;
import com.vethd.backend.DTOs.response.PetResponse;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class PetController {

    private static final UUID PROPIETARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final PetService petService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse register(@Valid @RequestBody PetRequest request) {
        return petService.register(PROPIETARIO_DEMO_ID, request);
    }

    @GetMapping
    public List<PetResponse> list() {
        return petService.listByOwner(PROPIETARIO_DEMO_ID);
    }

    @PutMapping("/{id}")
    public PetResponse update(@PathVariable UUID id, @Valid @RequestBody PetRequest request) {
        return petService.update(PROPIETARIO_DEMO_ID, id, request);
    }
}
