package com.vethd.backend.controller;

import com.vethd.backend.services.VetScheduleService;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
public class VetScheduleController {
    private final VetScheduleService vetScheduleService;
    private static final UUID VETERINARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public VetScheduleController(VetScheduleService vetScheduleService) {
        this.vetScheduleService = vetScheduleService;
    }
}