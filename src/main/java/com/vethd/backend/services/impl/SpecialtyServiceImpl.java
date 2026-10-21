package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.SpecialtyResponse;
import com.vethd.backend.mapper.SpecialtyMapper;
import com.vethd.backend.repository.SpecialtyRepository;
import com.vethd.backend.services.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public List<SpecialtyResponse> listActive() {
        return specialtyRepository.findByActiveTrue().stream()
                .map(SpecialtyMapper::toResponse)
                .toList();
    }
}
