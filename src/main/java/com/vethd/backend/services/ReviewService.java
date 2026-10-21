package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.ReviewRequest;
import com.vethd.backend.DTOs.response.ReviewResponse;
import com.vethd.backend.DTOs.response.VeterinarianReviewsResponse;
import com.vethd.backend.model.Review;
import com.vethd.backend.model.Appointment;
// import com.vethd.backend.repository.AppointmentRepository; 
import com.vethd.backend.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    // private final AppointmentRepository appointmentRepository; 

    public ReviewService(ReviewRepository reviewRepository /*, AppointmentRepository appointmentRepository*/) {
        this.reviewRepository = reviewRepository;
        // this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public ReviewResponse createReview(UUID appointmentId, ReviewRequest request, UUID currentUserId) {
        
        /* Descomentar esta línea cuando se suba el AppointmentRepository
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        */
        
        Appointment appointment = null; 
        if (appointment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada (Pendiente de repositorio)");
        }

        // VALIDACIÓN (CRITERIOS DE US-34)
    

        // 1. Validar que el usuario autenticado sea el propietario dueño (HTTP 403)
        UUID ownerUserId = appointment.getPet().getOwner().getUser().getId();
        if (!ownerUserId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para calificar esta cita");
        }

        // 2. Validar que la cita esté en estado ATENDIDA (HTTP 400)
        if (!"ATENDIDA".equalsIgnoreCase(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden calificar citas en estado ATENDIDA");
        }

        // 3. Validar que no exista una valoración previa para esta cita (HTTP 409)
        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta cita ya cuenta con una valoración registrada");
        }

        // 4. Guardar la valoración
        Review review = Review.builder()
                .appointment(appointment)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.builder()
                .id(savedReview.getId())
                .rating(savedReview.getRating())
                .comment(savedReview.getComment())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public VeterinarianReviewsResponse getVeterinarianReviews(UUID veterinarianId) {
        Double rawAverage = reviewRepository.findAverageRatingByVeterinarianId(veterinarianId);
        Double average = (rawAverage != null) ? Math.round(rawAverage * 100.0) / 100.0 : 0.0;

        List<Review> reviews = reviewRepository.findByVeterinarianId(veterinarianId);

        List<ReviewResponse> reviewResponses = reviews.stream().map(r -> 
            ReviewResponse.builder()
                    .id(r.getId())
                    .rating(r.getRating())
                    .comment(r.getComment())
                    .createdAt(r.getCreatedAt())
                    .build()
        ).collect(Collectors.toList());

        return VeterinarianReviewsResponse.builder()
                .promedio(average)
                .totalValoraciones(reviewResponses.size())
                .valoraciones(reviewResponses)
                .build();
    }
}