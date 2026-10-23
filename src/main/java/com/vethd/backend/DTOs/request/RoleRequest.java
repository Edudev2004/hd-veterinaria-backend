
package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleRequest(

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 50, message = "Maximo 50 caracteres")
        String name,

        @Size(max = 200, message = "Maximo 200 caracteres")
        String description,

        @NotNull(message = "Debe indicar los permisos del rol")
        Set<
                @NotBlank(message = "El codigo del permiso no puede estar vacio")
                String
        > permissions

) {}
