package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.ApprovalStatus;
import com.bookingcare.package_service.entity.HealthCheckPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HealthCheckPackageRepository extends JpaRepository<HealthCheckPackage, UUID> {

    Optional<HealthCheckPackage> findBySlugAndDeletedFalse(String slug);

    Optional<HealthCheckPackage> findByIdAndDeletedFalse(UUID id);

    List<HealthCheckPackage> findAllByStatusAndDeletedFalse(ApprovalStatus status);

    List<HealthCheckPackage> findAllByStatus(ApprovalStatus status);

    List<HealthCheckPackage> findAllByManagingDoctorIdAndDeletedFalse(String managingDoctorId);
}
