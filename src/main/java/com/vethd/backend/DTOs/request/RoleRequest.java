package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "Maximo 50 caracteres")
        String name,

        @Size(max = 200, message = "Maximo 200 caracteres")
        String description

) {}
