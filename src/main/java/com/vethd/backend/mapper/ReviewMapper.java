package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.ReviewRequest;
import com.vethd.backend.DTOs.response.ReviewResponse;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Review;

public class ReviewMapper {

    private ReviewMapper() {

    }

    public static Review toEntity(ReviewRequest request, Appointment appointment) {
        Review review = new Review();
        review.setAppointment(appointment);
        review.setRating(request.rating());
        review.setComment(request.comment());
        return review;
    }

    public static ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
