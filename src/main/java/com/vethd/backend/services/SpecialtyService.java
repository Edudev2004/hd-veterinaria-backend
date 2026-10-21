package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.SpecialtyResponse;

import java.util.List;

public interface SpecialtyService {

    List<SpecialtyResponse> listActive();
}
