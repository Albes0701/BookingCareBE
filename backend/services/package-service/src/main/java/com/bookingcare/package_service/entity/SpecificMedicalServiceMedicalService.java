package com.bookingcare.package_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "specific_medical_service_medical_service")
public class SpecificMedicalServiceMedicalService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @EqualsAndHashCode.Include
    private SpecificMedicalServiceMedicalServiceId id = new SpecificMedicalServiceMedicalServiceId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("specificMedicalServiceId")
    @JoinColumn(name = "specific_medical_service_id", nullable = false)
    private SpecificMedicalService specificMedicalService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("medicalServiceId")
    @JoinColumn(name = "medical_service_id", nullable = false)
    private MedicalService medicalService;

    @Column(name = "is_specific_medical_service_main_belong_to", nullable = false)
    private boolean specificMedicalServiceMainBelongTo = false;
}
