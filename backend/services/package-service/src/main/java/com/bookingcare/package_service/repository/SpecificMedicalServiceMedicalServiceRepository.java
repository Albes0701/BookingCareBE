package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.MedicalService;
import com.bookingcare.package_service.entity.SpecificMedicalService;
import com.bookingcare.package_service.entity.SpecificMedicalServiceMedicalService;
import com.bookingcare.package_service.entity.SpecificMedicalServiceMedicalServiceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecificMedicalServiceMedicalServiceRepository extends JpaRepository<SpecificMedicalServiceMedicalService, SpecificMedicalServiceMedicalServiceId> {

    List<SpecificMedicalServiceMedicalService> findAllBySpecificMedicalService(SpecificMedicalService specificMedicalService);

    List<SpecificMedicalServiceMedicalService> findAllByMedicalService(MedicalService medicalService);
}
