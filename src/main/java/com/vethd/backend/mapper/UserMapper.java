package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.UserRequest;
import com.vethd.backend.DTOs.response.UserResponse;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;

public class UserMapper {

    private UserMapper() {

    }

    public static User toEntity(UserRequest request, Role role, String passwordHash) {
        User user = new User();
        user.setDni(request.dni());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

    public static void updateEntity(UserRequest request, User user, Role role) {
        user.setDni(request.dni());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setRole(role);
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .dni(user.getDni())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
