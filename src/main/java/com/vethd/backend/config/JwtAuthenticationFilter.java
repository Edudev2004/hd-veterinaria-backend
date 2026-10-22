package com.vethd.backend.config;

import com.vethd.backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (jwtService.isTokenValid(token)) {
                try {
                    String username = jwtService.extractUsername(token);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        String rawRole = jwtService.extractRole(token);
                        List<GrantedAuthority> authorities = mapRoleToAuthorities(rawRole);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (Exception e) {
                    log.error("Error al procesar el token JWT: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> mapRoleToAuthorities(String rawRole) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (rawRole == null || rawRole.isBlank()) {
            return authorities;
        }

        String normalized = rawRole.trim().toUpperCase();

        if (normalized.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority(normalized));
            authorities.add(new SimpleGrantedAuthority(normalized.substring(5)));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
            authorities.add(new SimpleGrantedAuthority(normalized));
        }

        // Mapeo bidireccional para garantizar soporte entre términos en español e inglés:
        if (normalized.contains("PROPIETARIO") || normalized.contains("OWNER")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PROPIETARIO"));
            authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
            authorities.add(new SimpleGrantedAuthority("PROPIETARIO"));
            authorities.add(new SimpleGrantedAuthority("OWNER"));
        }
        if (normalized.contains("VETERINARIO") || normalized.contains("VET")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_VETERINARIO"));
            authorities.add(new SimpleGrantedAuthority("ROLE_VET"));
            authorities.add(new SimpleGrantedAuthority("VETERINARIO"));
            authorities.add(new SimpleGrantedAuthority("VET"));
        }
        if (normalized.contains("ADMIN")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }

        return authorities.stream().distinct().toList();
    }
}
