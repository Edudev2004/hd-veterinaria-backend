package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.MedicalHistoryEntryResponse;
import com.vethd.backend.DTOs.response.MedicalHistoryResponse;
import com.vethd.backend.mapper.MedicalHistoryMapper;
import com.vethd.backend.model.Diagnosis;
import com.vethd.backend.model.Pet;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.DiagnosisRepository;
import com.vethd.backend.repository.PetRepository;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.PetHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PetHistoryServiceImpl implements PetHistoryService {

    private final PetRepository petRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    @Override
    public MedicalHistoryResponse getPetMedicalHistory(UUID petId, String busqueda, UUID veterinarioId, LocalDate fechaDesde, LocalDate fechaHasta) {
        Pet pet = validateAccessAndGetPet(petId);
        List<MedicalHistoryEntryResponse> entries = getFilteredEntries(petId, busqueda, veterinarioId, fechaDesde, fechaHasta);
        return MedicalHistoryMapper.toResponse(pet, entries);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MedicalHistoryEntryResponse> getPetMedicalHistoryEntries(UUID petId, String busqueda, UUID veterinarioId, LocalDate fechaDesde, LocalDate fechaHasta) {
        validateAccessAndGetPet(petId);
        return getFilteredEntries(petId, busqueda, veterinarioId, fechaDesde, fechaHasta);
    }

    private Pet validateAccessAndGetPet(UUID petId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        boolean isOwner = pet.getOwner() != null &&
                pet.getOwner().getUser() != null &&
                pet.getOwner().getUser().getId().equals(currentUserId);

        boolean isVet = veterinarianRepository.findByUserId(currentUserId).isPresent() ||
                "VETERINARIO".equalsIgnoreCase(roleName) ||
                "ROLE_VET".equalsIgnoreCase(roleName) ||
                "ROLE_VETERINARIO".equalsIgnoreCase(roleName);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(roleName) ||
                "ROLE_ADMIN".equalsIgnoreCase(roleName);

        if (!isOwner && !isVet && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para consultar el historial de esta mascota");
        }

        return pet;
    }

    private List<MedicalHistoryEntryResponse> getFilteredEntries(UUID petId, String busqueda, UUID veterinarioId, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Diagnosis> diagnoses = diagnosisRepository.findByPetIdOrderByScheduledAtDesc(petId);

        Stream<Diagnosis> stream = diagnoses.stream();

        if (veterinarioId != null) {
            stream = stream.filter(d -> d.getAppointment().getVeterinarian().getId().equals(veterinarioId));
        }

        if (fechaDesde != null) {
            stream = stream.filter(d -> !d.getAppointment().getScheduledAt().toLocalDate().isBefore(fechaDesde));
        }

        if (fechaHasta != null) {
            stream = stream.filter(d -> !d.getAppointment().getScheduledAt().toLocalDate().isAfter(fechaHasta));
        }

        if (busqueda != null && !busqueda.isBlank()) {
            String q = busqueda.trim().toLowerCase();
            stream = stream.filter(d -> {
                String diag = d.getDiagnosis() != null ? d.getDiagnosis().toLowerCase() : "";
                String treat = d.getTreatment() != null ? d.getTreatment().toLowerCase() : "";
                String notes = d.getNotes() != null ? d.getNotes().toLowerCase() : "";
                String details = d.getDetails() != null ? d.getDetails().toLowerCase() : "";
                String reason = (d.getAppointment() != null && d.getAppointment().getReason() != null)
                        ? d.getAppointment().getReason().toLowerCase() : "";
                String vetName = (d.getAppointment() != null && d.getAppointment().getVeterinarian() != null && d.getAppointment().getVeterinarian().getUser() != null)
                        ? (d.getAppointment().getVeterinarian().getUser().getFirstName() + " " + d.getAppointment().getVeterinarian().getUser().getLastName()).toLowerCase()
                        : "";

                return diag.contains(q) || treat.contains(q) || notes.contains(q) || details.contains(q) || reason.contains(q) || vetName.contains(q);
            });
        }

        return stream.map(MedicalHistoryMapper::toEntryResponse).toList();
    }
}

