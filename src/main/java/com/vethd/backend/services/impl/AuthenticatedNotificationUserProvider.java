package com.vethd.backend.services.impl;

import com.vethd.backend.services.AuthenticatedUserProvider;
import com.vethd.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthenticatedNotificationUserProvider implements AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public AuthenticatedNotificationUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof String email)
                || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No existe una identidad autenticada válida");
        }

        return userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El usuario autenticado no existe"));
    }
}
