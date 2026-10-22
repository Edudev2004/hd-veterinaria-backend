package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.AppointmentCancelRequest;
import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentRescheduledRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.mapper.AppointmentMapper;
import com.vethd.backend.model.*;
import com.vethd.backend.repository.*;
import com.vethd.backend.services.AppointmentService;
import com.vethd.backend.services.CurrentUserProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
    private final DiagnosisRepository diagnosisRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
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
        validateSlotIsFree(request.veterinarianId(), scheduledAt, null);

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

    @Transactional
    @Override
    public AppointmentResponse cancelAppointment(UUID id, AppointmentCancelRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getPet().getOwner().getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this appointment");
        }

        if (!STATUS_PENDING.equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only pending appointments can be cancelled");
        }

        AppointmentMapper.cancelEntity(appointment, request.cancellationReason());
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponse(saved);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public List<AppointmentResponse> getMyAppointments(String estado, UUID mascotaId, LocalDate fecha, String busqueda) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        boolean isOwner = ownerRepository.findByUserId(currentUserId).isPresent() ||
                "PROPIETARIO".equalsIgnoreCase(roleName) ||
                "ROLE_OWNER".equalsIgnoreCase(roleName) ||
                "ROLE_PROPIETARIO".equalsIgnoreCase(roleName);

        boolean isVet = veterinarianRepository.findByUserId(currentUserId).isPresent() ||
                "VETERINARIO".equalsIgnoreCase(roleName) ||
                "ROLE_VET".equalsIgnoreCase(roleName) ||
                "ROLE_VETERINARIO".equalsIgnoreCase(roleName);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(roleName) ||
                "ROLE_ADMIN".equalsIgnoreCase(roleName);

        List<Appointment> appointments;
        if (isOwner) {
            appointments = appointmentRepository.findByPetOwnerUserIdOrderByScheduledAtDesc(currentUserId);
        } else if (isVet && !isAdmin) {
            appointments = appointmentRepository.findByVeterinarianUserIdOrderByScheduledAtDesc(currentUserId);
        } else {
            appointments = appointmentRepository.findAllWithDetailsOrderByScheduledAtDesc();
        }

        Stream<Appointment> stream = appointments.stream();

        if (estado != null && !estado.isBlank() && !"TODAS".equalsIgnoreCase(estado) && !"TODOS".equalsIgnoreCase(estado)) {
            String targetStatus = estado.trim().toUpperCase();
            stream = stream.filter(a -> a.getStatus().equalsIgnoreCase(targetStatus));
        }

        if (mascotaId != null) {
            stream = stream.filter(a -> a.getPet() != null && a.getPet().getId().equals(mascotaId));
        }

        if (fecha != null) {
            stream = stream.filter(a -> a.getScheduledAt().toLocalDate().equals(fecha));
        }

        if (busqueda != null && !busqueda.isBlank()) {
            String q = busqueda.trim().toLowerCase();
            stream = stream.filter(a -> {
                String petName = a.getPet() != null ? a.getPet().getName().toLowerCase() : "";
                String species = (a.getPet() != null && a.getPet().getSpecies() != null) ? a.getPet().getSpecies().toLowerCase() : "";
                String breed = (a.getPet() != null && a.getPet().getBreed() != null) ? a.getPet().getBreed().toLowerCase() : "";
                String vetFirst = (a.getVeterinarian() != null && a.getVeterinarian().getUser() != null) ? a.getVeterinarian().getUser().getFirstName().toLowerCase() : "";
                String vetLast = (a.getVeterinarian() != null && a.getVeterinarian().getUser() != null) ? a.getVeterinarian().getUser().getLastName().toLowerCase() : "";
                String vetFull = vetFirst + " " + vetLast;
                String specialty = (a.getSpecialty() != null && a.getSpecialty().getName() != null) ? a.getSpecialty().getName().toLowerCase() : "";
                String reason = a.getReason() != null ? a.getReason().toLowerCase() : "";
                String idStr = a.getId() != null ? a.getId().toString().toLowerCase() : "";

                return petName.contains(q) || species.contains(q) || breed.contains(q) ||
                        vetFirst.contains(q) || vetLast.contains(q) || vetFull.contains(q) ||
                        specialty.contains(q) || reason.contains(q) || idStr.contains(q);
            });
        }

        return stream.map(AppointmentMapper::toResponse).toList();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public AppointmentResponse getAppointmentById(UUID id) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .or(() -> appointmentRepository.findById(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        boolean isOwner = appointment.getPet().getOwner().getUser().getId().equals(currentUserId);
        boolean isAssignedVet = appointment.getVeterinarian().getUser().getId().equals(currentUserId);
        boolean isVet = veterinarianRepository.findByUserId(currentUserId).isPresent() ||
                "VETERINARIO".equalsIgnoreCase(roleName) ||
                "ROLE_VET".equalsIgnoreCase(roleName) ||
                "ROLE_VETERINARIO".equalsIgnoreCase(roleName);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(roleName) ||
                "ROLE_ADMIN".equalsIgnoreCase(roleName);

        if (!isOwner && !isAssignedVet && !isVet && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para ver esta cita");
        }

        Diagnosis diagnosis = diagnosisRepository.findByAppointment_Id(id).orElse(null);
        return AppointmentMapper.toResponse(appointment, diagnosis);
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
