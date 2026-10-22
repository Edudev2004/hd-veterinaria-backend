package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.OwnerRequest;
import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerServiceImplTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private OwnerServiceImpl ownerService;

    private Owner buildOwner(UUID id) {
        User user = User.builder()
                .firstName("Carlos")
                .lastName("Perez")
                .email("carlos@vethd.com")
                .photoUrl("https://img.test/carlos.jpg")
                .build();
        return Owner.builder()
                .id(id)
                .phone("987654321")
                .address("Av. Lima 123")
                .user(user)
                .build();
    }

    @Test
    void getProfileDevuelveLosDatosDelPropietario() {
        UUID id = UUID.randomUUID();
        when(ownerRepository.findById(id)).thenReturn(Optional.of(buildOwner(id)));

        OwnerResponse response = ownerService.getProfile(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.firstName()).isEqualTo("Carlos");
        assertThat(response.email()).isEqualTo("carlos@vethd.com");
        assertThat(response.phone()).isEqualTo("987654321");
        assertThat(response.photoUrl()).isEqualTo("https://img.test/carlos.jpg");
    }

    @Test
    void getProfileLanza404SiElPropietarioNoExiste() {
        UUID id = UUID.randomUUID();
        when(ownerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.getProfile(id))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void updateProfileCambiaNombreTelefonoYFotoSinTocarLaDireccion() {
        UUID id = UUID.randomUUID();
        when(ownerRepository.findById(id)).thenReturn(Optional.of(buildOwner(id)));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        OwnerRequest request = OwnerRequest.builder()
                .firstName("Luis")
                .phone("999888777")
                .photoUrl("https://img.test/luis.jpg")
                .build();

        OwnerResponse response = ownerService.updateProfile(id, request);

        assertThat(response.firstName()).isEqualTo("Luis");
        assertThat(response.lastName()).isEqualTo("Perez");
        assertThat(response.phone()).isEqualTo("999888777");
        assertThat(response.address()).isEqualTo("Av. Lima 123");
        assertThat(response.photoUrl()).isEqualTo("https://img.test/luis.jpg");
    }

    @Test
    void updateProfileConFotoEnBlancoBorraLaFoto() {
        UUID id = UUID.randomUUID();
        when(ownerRepository.findById(id)).thenReturn(Optional.of(buildOwner(id)));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        OwnerResponse response = ownerService.updateProfile(id, OwnerRequest.builder().photoUrl("  ").build());

        assertThat(response.photoUrl()).isNull();
    }

    @Test
    void updateProfileLanza404SiElPropietarioNoExisteYNoGuarda() {
        UUID id = UUID.randomUUID();
        when(ownerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.updateProfile(id, OwnerRequest.builder().phone("999888777").build()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
        verify(ownerRepository, never()).save(any());
    }
}
