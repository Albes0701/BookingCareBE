package com.bookingcare.clinic.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "clinic_branch_healthcheck_packages")
public class ClinicBranchHealthcheckPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_branch_id", nullable = false)
    private ClinicBranch clinicBranch;

    @Column(name = "healthcheck_package_id", length = 255, nullable = false)
    private String healthcheckPackageId;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
}
