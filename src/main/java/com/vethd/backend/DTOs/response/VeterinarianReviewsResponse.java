package com.vethd.backend.DTOs.response;

import lombok.Builder;
import java.util.List;

@Builder
public record VeterinarianReviewsResponse(
        Double promedio,
        Integer totalValoraciones,
        List<ReviewResponse> valoraciones
) {
}