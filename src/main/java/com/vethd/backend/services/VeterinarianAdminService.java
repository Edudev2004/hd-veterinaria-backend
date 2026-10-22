package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.VeterinarianCreateRequest;
import com.vethd.backend.DTOs.request.VeterinarianUpdateRequest;
import com.vethd.backend.DTOs.response.AdminVeterinarianResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VeterinarianAdminService {
    Page<AdminVeterinarianResponse> listar(String nombre, String estado, Pageable pageable);
    AdminVeterinarianResponse registrar(VeterinarianCreateRequest dto);
    AdminVeterinarianResponse editar(UUID id, VeterinarianUpdateRequest dto);
    AdminVeterinarianResponse cambiarEstado(UUID id);
}
