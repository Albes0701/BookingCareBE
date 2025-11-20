package com.bookingcare.package_service.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "health_check_package_specialty")
public class HealthCheckPackageSpecialty implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @EqualsAndHashCode.Include
    private HealthCheckPackageSpecialtyId id = new HealthCheckPackageSpecialtyId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("packageId")
    @JoinColumn(name = "package_id", nullable = false)
    private HealthCheckPackage healthCheckPackage;

    public UUID getSpecialtyId() {
        return id != null ? id.getSpecialtyId() : null;
    }

    public void setSpecialtyId(UUID specialtyId) {
        if (id == null) {
            id = new HealthCheckPackageSpecialtyId();
        }
        id.setSpecialtyId(specialtyId);
    }
}
