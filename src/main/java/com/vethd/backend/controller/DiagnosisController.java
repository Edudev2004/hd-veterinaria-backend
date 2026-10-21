package com.vethd.backend.controller;

import com.vethd.backend.services.DiagnosisService;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
public class DiagnosisController {
    private final DiagnosisService diagnosisService;
    private static final UUID VETERINARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }
}