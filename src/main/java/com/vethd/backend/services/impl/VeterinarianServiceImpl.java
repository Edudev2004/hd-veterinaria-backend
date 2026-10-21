package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.VeterinarianResponse;
import com.vethd.backend.mapper.VeterinarianMapper;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.VeterinarianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinarianServiceImpl implements VeterinarianService {

    private final VeterinarianRepository veterinarianRepository;


    @Override
    public List<VeterinarianResponse> listActive(UUID specialtyId) {
        List<Veterinarian> veterinarians = (specialtyId == null)
                ? veterinarianRepository.findActiveAll()
                : veterinarianRepository.findActiveBySpecialty(specialtyId);

        Map<UUID, Double> ratings = veterinarianRepository.findAverageRatings().stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Double) row[1]
                ));

        return veterinarians.stream()
                .map(vet -> VeterinarianMapper.toResponse(
                        vet, ratings.getOrDefault(vet.getId(), 0.0))
                ).toList();
    }
}
