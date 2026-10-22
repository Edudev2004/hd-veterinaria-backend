package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OwnerRequest(
        String firstName,
        String lastName,
        @Size(min = 7, max = 20, message = "Phone must have between 7 and 20 characters")
        String phone,
        String address,
        String photoUrl
) {
}
