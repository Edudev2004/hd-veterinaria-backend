package com.vethd.backend.services.impl;

import com.vethd.backend.services.CurrentUserProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeaderCurrentUserProvider implements CurrentUserProvider {

    private static final String HEADER = "X-User-Id";
    private final HttpServletRequest request;



    // ESTA IMPLEMENTACION SE BORRARA CUANDO ENTRE JWT
    @Override
    public UUID getCurrentUserId() {
        String value = request.getHeader(HEADER);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }

        return UUID.fromString(value);
    }
}
