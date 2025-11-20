package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.HealthCheckPackage;
import com.bookingcare.package_service.entity.SpecificMedicalService;
import com.bookingcare.package_service.entity.SpecificMedicalServiceHealthCheckPackage;
import com.bookingcare.package_service.entity.SpecificMedicalServiceHealthCheckPackageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecificMedicalServiceHealthCheckPackageRepository extends JpaRepository<SpecificMedicalServiceHealthCheckPackage, SpecificMedicalServiceHealthCheckPackageId> {

    List<SpecificMedicalServiceHealthCheckPackage> findAllBySpecificMedicalService(SpecificMedicalService specificMedicalService);

    List<SpecificMedicalServiceHealthCheckPackage> findAllByHealthCheckPackage(HealthCheckPackage healthCheckPackage);
}
