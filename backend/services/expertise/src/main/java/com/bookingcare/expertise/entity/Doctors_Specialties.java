package com.bookingcare.expertise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(
        name = "doctors_specialties",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_specialty", columnNames = {"doctor_id", "specialty_id"})
        },
        indexes = {
                @Index(name = "idx_ds_doctor", columnList = "doctor_id"),
                @Index(name = "idx_ds_specialty", columnList = "specialty_id")
        }
)
public class Doctors_Specialties {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctors;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialties specialties;

}
