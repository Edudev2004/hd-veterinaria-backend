package com.vethd.backend.DTOs.response;

import java.util.UUID;

public record UserRoleResponse(
        UUID userId,
        String email,
        UUID roleId,
        String roleName
) {}
