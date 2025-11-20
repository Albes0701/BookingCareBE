package com.bookingcare.package_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class SpecificMedicalServiceMedicalServiceId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "specific_medical_service_id", nullable = false)
    private UUID specificMedicalServiceId;

    @Column(name = "medical_service_id", nullable = false)
    private UUID medicalServiceId;
}
