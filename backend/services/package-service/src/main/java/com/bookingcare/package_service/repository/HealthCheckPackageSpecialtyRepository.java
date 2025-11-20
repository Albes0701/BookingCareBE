package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.HealthCheckPackage;
import com.bookingcare.package_service.entity.HealthCheckPackageSpecialty;
import com.bookingcare.package_service.entity.HealthCheckPackageSpecialtyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HealthCheckPackageSpecialtyRepository extends JpaRepository<HealthCheckPackageSpecialty, HealthCheckPackageSpecialtyId> {

    List<HealthCheckPackageSpecialty> findAllByHealthCheckPackage(HealthCheckPackage healthCheckPackage);

    List<HealthCheckPackageSpecialty> findAllByIdSpecialtyId(UUID specialtyId);
}
