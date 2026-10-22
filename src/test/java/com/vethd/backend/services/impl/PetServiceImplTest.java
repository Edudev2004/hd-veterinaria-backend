package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.PetRequest;
import com.vethd.backend.DTOs.response.PetResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Pet;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetServiceImpl petService;

    private PetRequest buildRequest(String name) {
        return PetRequest.builder()
                .name(name)
                .species("perro")
                .breed("Labrador")
                .gender("macho")
                .build();
    }

    private Pet buildPet(UUID petId, Owner owner, String name) {
        return Pet.builder()
                .id(petId)
                .owner(owner)
                .name(name)
                .species("perro")
                .gender("macho")
                .build();
    }

    @Test
    void registerGuardaLaMascotaVinculadaAlPropietario() {
        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder().id(ownerId).build();
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        PetResponse response = petService.register(ownerId, buildRequest("Firulais"));

        assertThat(response.name()).isEqualTo("Firulais");
        assertThat(response.species()).isEqualTo("perro");
        assertThat(response.breed()).isEqualTo("Labrador");
        assertThat(response.active()).isTrue();
    }

    @Test
    void registerLanza404SiElPropietarioNoExisteYNoGuarda() {
        UUID ownerId = UUID.randomUUID();
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.register(ownerId, buildRequest("Firulais")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
        verify(petRepository, never()).save(any());
    }

    @Test
    void listByOwnerDevuelveSoloLasMascotasDelPropietario() {
        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder().id(ownerId).build();
        when(petRepository.findByOwner_IdAndActiveTrueOrderByCreatedAtDesc(ownerId))
                .thenReturn(List.of(buildPet(UUID.randomUUID(), owner, "Luna"), buildPet(UUID.randomUUID(), owner, "Max")));

        List<PetResponse> result = petService.listByOwner(ownerId);

        assertThat(result).extracting(PetResponse::name).containsExactly("Luna", "Max");
    }

    @Test
    void updateModificaLaMascotaDelDueno() {
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        Pet pet = buildPet(petId, Owner.builder().id(ownerId).build(), "Firulais");
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        PetResponse response = petService.update(ownerId, petId, buildRequest("Firulais Editado"));

        assertThat(response.id()).isEqualTo(petId);
        assertThat(response.name()).isEqualTo("Firulais Editado");
    }

    @Test
    void updateLanza404SiLaMascotaNoExiste() {
        UUID petId = UUID.randomUUID();
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.update(UUID.randomUUID(), petId, buildRequest("X")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void updateLanza403SiLaMascotaEsDeOtroPropietarioYNoGuarda() {
        UUID petId = UUID.randomUUID();
        Pet petAjena = buildPet(petId, Owner.builder().id(UUID.randomUUID()).build(), "Ajena");
        when(petRepository.findById(petId)).thenReturn(Optional.of(petAjena));

        assertThatThrownBy(() -> petService.update(UUID.randomUUID(), petId, buildRequest("Hackeada")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(403));
        verify(petRepository, never()).save(any());
        assertThat(petAjena.getName()).isEqualTo("Ajena");
    }
}
