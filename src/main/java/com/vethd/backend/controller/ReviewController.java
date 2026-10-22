package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.ReviewRequest;
import com.vethd.backend.DTOs.response.ReviewResponse;
import com.vethd.backend.DTOs.response.VeterinarianReviewsResponse;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    // 1. Endpoint para calificar una atención (POST /api/citas/{id}/valoracion)
    @PostMapping("/citas/{id}/valoracion")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable("id") UUID appointmentId,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID headerUserId
    ) {
        UUID currentUserId = headerUserId != null ? headerUserId : currentUserProvider.getCurrentUserId();
        ReviewResponse response = reviewService.createReview(appointmentId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Endpoint para ver las valoraciones de un veterinario (GET /api/veterinarios/{id}/valoraciones)
    @GetMapping("/veterinarios/{id}/valoraciones")
    public ResponseEntity<VeterinarianReviewsResponse> getVeterinarianReviews(
            @PathVariable("id") UUID veterinarianId
    ) {
        VeterinarianReviewsResponse response = reviewService.getVeterinarianReviews(veterinarianId);
        return ResponseEntity.ok(response);
    }
}