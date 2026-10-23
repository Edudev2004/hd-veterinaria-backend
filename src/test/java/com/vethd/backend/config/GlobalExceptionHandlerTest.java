package com.vethd.backend.config;

import com.vethd.backend.DTOs.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test-endpoint");
    }

    @Test
    @DisplayName("Debe manejar ResponseStatusException retornando el status y motivo correspondientes")
    void handleResponseStatusException_ShouldReturnStatusAndReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Veterinario no encontrado", response.getBody().getMessage());
        assertEquals("/api/test-endpoint", response.getBody().getPath());
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException con los errores de campo estructurados")
    void handleValidationException_ShouldReturnValidationErrors() {
        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("userDto", "email", "Formato de correo no válido");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Formato de correo no válido", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals("Formato de correo no válido", response.getBody().getValidationErrors().get("email"));
    }

    @Test
    @DisplayName("Debe manejar ConstraintViolationException retornando 400 y detalles")
    void handleConstraintViolation_ShouldReturnBadRequest() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("puntuacion");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("debe ser mayor o igual a 1");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals("debe ser mayor o igual a 1", response.getBody().getValidationErrors().get("puntuacion"));
    }

    @Test
    @DisplayName("Debe manejar BadCredentialsException con 401 Unauthorized")
    void handleBadCredentials_ShouldReturn401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Credenciales incorrectas", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar AuthenticationException con 401 Unauthorized")
    void handleAuthenticationException_ShouldReturn401() {
        AuthenticationException ex = Mockito.mock(AuthenticationException.class);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No autenticado o token inválido", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar AccessDeniedException con 403 Forbidden")
    void handleAccessDenied_ShouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("No tienes permisos para realizar esta acción", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar DataIntegrityViolationException con 409 Conflict")
    void handleDataIntegrity_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Unique index violation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrity(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Conflicto de integridad de datos"));
    }

    @Test
    @DisplayName("Debe manejar IllegalArgumentException con 400 Bad Request")
    void handleIllegalArgument_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Parámetro inválido");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Parámetro inválido", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar HttpRequestMethodNotSupportedException con 405 Method Not Allowed")
    void handleMethodNotSupported_ShouldReturn405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("POST"));
    }

    @Test
    @DisplayName("Debe manejar NoResourceFoundException con 404 Not Found")
    void handleNoResourceFound_ShouldReturn404() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoResourceFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("El recurso solicitado no existe", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar MaxUploadSizeExceededException con 400 Bad Request")
    void handleMaxSize_ShouldReturn400() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(10 * 1024 * 1024);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxSize(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("10MB"));
    }

    @Test
    @DisplayName("Debe manejar excepciones generales con 500 Internal Server Error sin exponer detalles internos")
    void handleGenericException_ShouldReturn500() {
        Exception ex = new RuntimeException("NullPointerException oculta");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Ocurrió un error interno en el servidor", response.getBody().getMessage());
    }
}
