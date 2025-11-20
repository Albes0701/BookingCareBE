package com.bookingcare.package_service.repository;

import com.bookingcare.package_service.entity.PackageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackageTypeRepository extends JpaRepository<PackageType, UUID> {

    Optional<PackageType> findBySlug(String slug);

    List<PackageType> findAllByDeletedFalse();
}
