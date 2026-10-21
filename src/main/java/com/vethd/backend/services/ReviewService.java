package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.ReviewRequest;
import com.vethd.backend.DTOs.response.ReviewResponse;
import com.vethd.backend.DTOs.response.VeterinarianReviewsResponse;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse createReview(UUID appointmentId, ReviewRequest request, UUID currentUserId);

    VeterinarianReviewsResponse getVeterinarianReviews(UUID veterinarianId);
}
