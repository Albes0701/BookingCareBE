package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.SpecificMedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecificMedicalServiceRepository extends JpaRepository<SpecificMedicalService, UUID> {

    Optional<SpecificMedicalService> findBySlug(String slug);

    List<SpecificMedicalService> findAllByDeletedFalse();

    Optional<SpecificMedicalService> findByIdAndDeletedFalse(UUID id);
}
