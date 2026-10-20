package com.vethd.backend.DTOs.request;

import lombok.Builder;

@Builder
public record OwnerRequest(
        String phone,
        String address
) {
}
