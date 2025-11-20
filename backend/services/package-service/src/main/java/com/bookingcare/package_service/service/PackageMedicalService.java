package com.bookingcare.package_service.service;

import com.bookingcare.package_service.dto.HealthCheckPackageDetailResponse;
import com.bookingcare.package_service.dto.HealthCheckPackageResponse;
import com.bookingcare.package_service.dto.MedicalServiceDetailResponse;
import com.bookingcare.package_service.dto.MedicalServiceResponse;
import com.bookingcare.package_service.dto.PackageTypeResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceResponse;
import com.bookingcare.package_service.entity.ApprovalStatus;
import com.bookingcare.package_service.entity.HealthCheckPackage;
import com.bookingcare.package_service.entity.MedicalService;
import com.bookingcare.package_service.exception.ApiException;
import com.bookingcare.package_service.exception.ErrorCode;
import com.bookingcare.package_service.mapper.PackageServiceMapper;
import com.bookingcare.package_service.repository.HealthCheckPackageRepository;
import com.bookingcare.package_service.repository.MedicalServiceRepository;
import com.bookingcare.package_service.repository.PackageTypeRepository;
import com.bookingcare.package_service.repository.SpecificMedicalServiceHealthCheckPackageRepository;
import com.bookingcare.package_service.repository.SpecificMedicalServiceMedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PackageMedicalService {

    private final HealthCheckPackageRepository healthCheckPackageRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final PackageTypeRepository packageTypeRepository;
    private final SpecificMedicalServiceMedicalServiceRepository specificMedicalServiceMedicalServiceRepository;
    private final SpecificMedicalServiceHealthCheckPackageRepository specificMedicalServiceHealthCheckPackageRepository;
    private final PackageServiceMapper packageServiceMapper;

    /**
     * List all approved health check packages for catalog pages.
     */
    public List<HealthCheckPackageResponse> getApprovedPackages() {
        return healthCheckPackageRepository
                .findAllByStatusAndDeletedFalse(ApprovalStatus.APPROVED)
                .stream()
                .map(packageServiceMapper::toHealthCheckPackageResponse)
                .toList();
    }

    /**
     * List all medical service groups for filters (active ones only).
     */
    public List<MedicalServiceResponse> getMedicalServicesTaxonomy() {
        return medicalServiceRepository
                .findAllByDeletedFalse()
                .stream()
                .map(packageServiceMapper::toMedicalServiceResponse)
                .toList();
    }

    /**
     * Fetch detailed information about a medical service along with its specific medical services.
     */
    public MedicalServiceDetailResponse getMedicalServiceDetail(UUID id) {
        MedicalService medicalService = getActiveMedicalService(id);

        return new MedicalServiceDetailResponse(
                packageServiceMapper.toMedicalServiceResponse(medicalService),
                mapSpecificsForMedicalService(medicalService)
        );
    }

    /**
     * Returns the list of specific medical services under a medical service group.
     */
    public List<SpecificMedicalServiceResponse> getSpecificsByMedicalService(UUID id) {
        MedicalService medicalService = getActiveMedicalService(id);
        return mapSpecificsForMedicalService(medicalService);
    }

    /**
     * Fetch detailed information about a health check package plus the specific medical services it includes.
     */
    public HealthCheckPackageDetailResponse getPackageDetail(UUID id) {
        HealthCheckPackage healthCheckPackage = healthCheckPackageRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PACKAGE_NOT_FOUND));

        List<SpecificMedicalServiceResponse> specifics = specificMedicalServiceHealthCheckPackageRepository
                .findAllByHealthCheckPackage(healthCheckPackage)
                .stream()
                .map(link -> link.getSpecificMedicalService())
                .filter(specific -> !specific.isDeleted())
                .map(packageServiceMapper::toSpecificMedicalServiceResponse)
                .toList();

        return new HealthCheckPackageDetailResponse(
                packageServiceMapper.toHealthCheckPackageResponse(healthCheckPackage),
                specifics
        );
    }

    /**
     * List all package types for filters/menu (active only).
     */
    public List<PackageTypeResponse> getPackageTypes() {
        return packageTypeRepository
                .findAllByDeletedFalse()
                .stream()
                .map(packageServiceMapper::toPackageTypeResponse)
                .toList();
    }

    /**
     * Fetch an active (non-deleted) medical service or throw if it does not exist.
     */
    private MedicalService getActiveMedicalService(UUID id) {
        return medicalServiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND));
    }

    /**
     * Retrieve all non-deleted specific medical services attached to the given medical service.
     */
    private List<SpecificMedicalServiceResponse> mapSpecificsForMedicalService(MedicalService medicalService) {
        return specificMedicalServiceMedicalServiceRepository
                .findAllByMedicalService(medicalService)
                .stream()
                .map(link -> link.getSpecificMedicalService())
                .filter(specific -> !specific.isDeleted())
                .map(packageServiceMapper::toSpecificMedicalServiceResponse)
                .toList();
    }

    /**
     * Fetch detailed information about a health check package by its slug.
     */    
    public HealthCheckPackageResponse getPackageDetailBySlug(String slug) {
        HealthCheckPackage healthCheckPackage = healthCheckPackageRepository
                .findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new ApiException(ErrorCode.PACKAGE_NOT_FOUND));

        return packageServiceMapper.toHealthCheckPackageResponse(healthCheckPackage);
        
    }
}
