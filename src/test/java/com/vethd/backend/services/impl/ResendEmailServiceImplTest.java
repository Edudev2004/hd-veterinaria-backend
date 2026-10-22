package com.vethd.backend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ResendEmailServiceImplTest {

    private ResendEmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new ResendEmailServiceImpl("test_key", "VetHD <test@vethd.com>");
    }

    @Test
    @DisplayName("En modo test/simulación sendPasswordResetEmail no lanza excepciones")
    void sendPasswordResetEmail_TestMode_DoesNotThrow() {
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "token123"));
    }

    @Test
    @DisplayName("Con apiKey nulo o vacío sendPasswordResetEmail maneja modo simulación")
    void sendPasswordResetEmail_EmptyApiKey_DoesNotThrow() {
        ResendEmailServiceImpl emptyKeyService = new ResendEmailServiceImpl("", "VetHD <test@vethd.com>");
        assertDoesNotThrow(() -> emptyKeyService.sendPasswordResetEmail("user@example.com", "token123"));
    }
}
