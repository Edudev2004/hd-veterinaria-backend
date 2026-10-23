package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserRoleRequest(

        @NotNull(message = "El rol es obligatorio")
        UUID roleId

) {}
