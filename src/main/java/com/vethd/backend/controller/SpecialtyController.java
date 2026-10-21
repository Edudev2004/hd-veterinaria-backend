package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.SpecialtyResponse;
import com.vethd.backend.services.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;


    @GetMapping("/especialidades")
    public ResponseEntity<List<SpecialtyResponse>> list(){
        return ResponseEntity.ok(specialtyService.listActive());
    }


}
