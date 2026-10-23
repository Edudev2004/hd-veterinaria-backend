
package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record SpecialtyStatusRequest(
        @NotNull Boolean active
) {
}
