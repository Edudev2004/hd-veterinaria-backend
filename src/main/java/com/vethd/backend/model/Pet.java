package com.vethd.backend.model;

import com.vethd.backend.util.GenderNormalizer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String species;
    @Column(length = 50)
    private String breed;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = true, name = "birthdate")
    private LocalDate birthDate;


    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Garantiza que cualquier escritura (API, inicializadores, etc.) cumpla
     * la restricción pets_gender_check ('MALE'/'FEMALE').
     */
    @PrePersist
    @PreUpdate
    private void normalizeGender() {
        this.gender = GenderNormalizer.toDatabase(this.gender);
    }
}
