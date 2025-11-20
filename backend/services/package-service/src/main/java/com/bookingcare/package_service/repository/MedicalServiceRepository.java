package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, UUID> {

    Optional<MedicalService> findBySlug(String slug);

    List<MedicalService> findAllByDeletedFalse();

    Optional<MedicalService> findByIdAndDeletedFalse(UUID id);
}
