package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentRescheduledRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.mapper.AppointmentMapper;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Pet;
import com.vethd.backend.model.Specialty;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.PetRepository;
import com.vethd.backend.repository.SpecialtyRepository;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.AppointmentService;
import com.vethd.backend.services.CurrentUserProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final String STATUS_PENDING = "PENDIENTE";
    private static final LocalTime START_HOUR = LocalTime.of(8, 0);
    private static final LocalTime END_HOUR = LocalTime.of(18, 0);

    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserProvider currentUserProvider;


    @Transactional
    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        Veterinarian veterinarian = veterinarianRepository.findById(request.veterinarianId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinarian not found"));

        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specialty not found"));

        if (!veterinarian.getSpecialties().contains(specialty)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Specialty does not belong to veterinarian");
        }

        if (!pet.getOwner().getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this pet");
        }

        validateWorkingHour(request.hour());

        LocalDateTime scheduledAt = LocalDateTime.of(request.date(), request.hour());
        validateFutureDateTime(scheduledAt);
        validateSlotIsFree(request.veterinarianId(),scheduledAt,null);

        Appointment appointment = AppointmentMapper.toEntity(request, pet, veterinarian, specialty);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponse(saved);

    }

    @Transactional
    @Override
    public AppointmentResponse rescheduleAppointment(UUID id, AppointmentRescheduledRequest request) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getPet().getOwner().getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this appointment");
        }

        if (!STATUS_PENDING.equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only pending appointments can be rescheduled");
        }

        Veterinarian targetVeterinarian = resolveTargetVeterinarian(request, appointment);

        validateWorkingHour(request.newHour());

        LocalDateTime scheduledAt = LocalDateTime.of(request.newDate(), request.newHour());
        validateFutureDateTime(scheduledAt);
        validateSlotIsFree(targetVeterinarian.getId(), scheduledAt, appointment.getId());

        AppointmentMapper.rescheduleEntity(request, appointment, targetVeterinarian);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponse(saved);
    }


    private Veterinarian resolveTargetVeterinarian(AppointmentRescheduledRequest request,
                                                   Appointment appointment) {
        if (request.newVeterinarianId() == null) {
            return appointment.getVeterinarian();
        }

        Veterinarian target = veterinarianRepository.findById(request.newVeterinarianId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinarian not found"));

        if (!target.getSpecialties().contains(appointment.getSpecialty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Specialty does not belong to veterinarian");
        }

        return target;
    }

    private void validateWorkingHour(LocalTime hour) {
        if (hour.isBefore(START_HOUR) || !hour.isBefore(END_HOUR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hour must be between 08:00 and 18:00");
        }
    }

    private void validateFutureDateTime(LocalDateTime scheduledAt) {
        if (!scheduledAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment must be in the future");
        }
    }

    private void validateSlotIsFree(UUID veterinarianId, LocalDateTime scheduledAt,
                                    UUID excludedAppointmentId) {
        boolean occupied = (excludedAppointmentId == null)
                ? appointmentRepository.existsByVeterinarian_IdAndScheduledAtAndStatus(
                veterinarianId, scheduledAt, STATUS_PENDING)
                : appointmentRepository.existsByVeterinarian_IdAndScheduledAtAndStatusAndIdNot(
                veterinarianId, scheduledAt, STATUS_PENDING, excludedAppointmentId);

        if (occupied) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot is not available");
        }
    }


}
