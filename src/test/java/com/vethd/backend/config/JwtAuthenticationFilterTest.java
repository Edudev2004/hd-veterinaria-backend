package com.vethd.backend.config;

import com.vethd.backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin header Authorization, continúa filterChain y no autentica")
    void doFilterInternal_NoHeader_DoesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Header sin prefijo Bearer, continúa filterChain y no autentica")
    void doFilterInternal_HeaderWithoutBearer_DoesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic 123456");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Token inválido, continúa filterChain y no autentica")
    void doFilterInternal_InvalidToken_DoesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token_invalido");
        when(jwtService.isTokenValid("token_invalido")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).isTokenValid("token_invalido");
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Token válido con rol PROPIETARIO, establece autenticación y mapea roles")
    void doFilterInternal_ValidTokenPropietario_AuthenticatesSuccessfully() throws Exception {
        String token = "valid_token_propietario";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("cliente@vethd.com");
        when(jwtService.extractRole(token)).thenReturn("PROPIETARIO");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("cliente@vethd.com", auth.getName());

        List<String> authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorityNames.contains("ROLE_PROPIETARIO"));
        assertTrue(authorityNames.contains("ROLE_OWNER"));
    }

    @Test
    @DisplayName("Token válido con rol VETERINARIO, establece autenticación y mapea roles")
    void doFilterInternal_ValidTokenVeterinario_AuthenticatesSuccessfully() throws Exception {
        String token = "valid_token_vet";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("vet@vethd.com");
        when(jwtService.extractRole(token)).thenReturn("VETERINARIO");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("vet@vethd.com", auth.getName());

        List<String> authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorityNames.contains("ROLE_VETERINARIO"));
        assertTrue(authorityNames.contains("ROLE_VET"));
    }

    @Test
    @DisplayName("Token válido con rol ADMIN, establece autenticación y mapea roles")
    void doFilterInternal_ValidTokenAdmin_AuthenticatesSuccessfully() throws Exception {
        String token = "valid_token_admin";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin@vethd.com");
        when(jwtService.extractRole(token)).thenReturn("ADMIN");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin@vethd.com", auth.getName());

        List<String> authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorityNames.contains("ROLE_ADMIN"));
    }
}
