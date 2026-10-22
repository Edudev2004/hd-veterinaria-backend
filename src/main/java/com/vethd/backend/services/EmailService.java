package com.vethd.backend.services;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}
