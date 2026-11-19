package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.ReviewRequest;
import com.vethd.backend.DTOs.response.ReviewResponse;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Pet;
import com.vethd.backend.model.Review;
import com.vethd.backend.model.User;

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
        Appointment appointment = review.getAppointment();
        Pet pet = appointment != null ? appointment.getPet() : null;
        User ownerUser = (pet != null && pet.getOwner() != null) ? pet.getOwner().getUser() : null;

        return ReviewResponse.builder()
                .id(review.getId())
                .appointmentId(appointment != null ? appointment.getId() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .petName(pet != null ? pet.getName() : null)
                .petPhotoUrl(pet != null ? pet.getPhotoUrl() : null)
                .ownerName(ownerUser != null
                        ? (ownerUser.getFirstName() + " " + ownerUser.getLastName()).trim()
                        : null)
                .build();
    }
}