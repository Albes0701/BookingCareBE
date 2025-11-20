package com.bookingcare.package_service.service;

import com.bookingcare.package_service.dto.MedicalServiceRequest;
import com.bookingcare.package_service.dto.MedicalServiceResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceRequest;
import com.bookingcare.package_service.dto.SpecificMedicalServiceResponse;
import com.bookingcare.package_service.entity.MedicalService;
import com.bookingcare.package_service.entity.SpecificMedicalService;
import com.bookingcare.package_service.exception.ApiException;
import com.bookingcare.package_service.exception.ErrorCode;
import com.bookingcare.package_service.mapper.PackageServiceMapper;
import com.bookingcare.package_service.repository.MedicalServiceRepository;
import com.bookingcare.package_service.repository.SpecificMedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MedicalTaxonomyService {

    private final MedicalServiceRepository medicalServiceRepository;
    private final SpecificMedicalServiceRepository specificMedicalServiceRepository;
    private final PackageServiceMapper packageServiceMapper;

    /**
     * Return every active medical service for administration listings.
     */
    @Transactional(readOnly = true)
    public List<MedicalServiceResponse> listMedicalServices() {
        return medicalServiceRepository.findAllByDeletedFalse()
                .stream()
                .map(packageServiceMapper::toMedicalServiceResponse)
                .toList();
    }

    /**
     * Admin listing that returns every medical service, including soft-deleted entries.
     */
    @Transactional(readOnly = true)
    public List<MedicalServiceResponse> listAllMedicalServices() {
        return medicalServiceRepository.findAll()
                .stream()
                .map(packageServiceMapper::toMedicalServiceResponse)
                .toList();
    }

    /**
     * Create a new top-level medical service (slug uniqueness enforced).
     */
    public MedicalServiceResponse createMedicalService(MedicalServiceRequest request) {
        ensureMedicalSlugAvailable(request.slug(), null);
        MedicalService entity = packageServiceMapper.toMedicalService(request);
        entity.setDeleted(false);
        MedicalService saved = medicalServiceRepository.save(entity);
        return packageServiceMapper.toMedicalServiceResponse(saved);
    }

    /**
     * Update an existing medical service, validating ownership of the slug.
     */
    public MedicalServiceResponse updateMedicalService(UUID id, MedicalServiceRequest request) {
        // requireAdmin();
        MedicalService entity = requireMedicalService(id);
        if (StringUtils.hasText(request.slug()) && !request.slug().equals(entity.getSlug())) {
            ensureMedicalSlugAvailable(request.slug(), id);
        }
        packageServiceMapper.updateMedicalServiceFromRequest(request, entity);
        return packageServiceMapper.toMedicalServiceResponse(entity);
    }

    /**
     * Soft delete a medical service so it no longer appears in catalogs.
     */
    public void deleteMedicalService(UUID id) {
        
        MedicalService entity = requireMedicalService(id);
        entity.setDeleted(true);
    }

    /**
     * Return all active specific medical services for administration listings.
     */
    @Transactional(readOnly = true)
    public List<SpecificMedicalServiceResponse> listSpecificMedicalServices() {
        return specificMedicalServiceRepository.findAllByDeletedFalse()
                .stream()
                .map(packageServiceMapper::toSpecificMedicalServiceResponse)
                .toList();
    }

    /**
     * Admin listing that returns every specific medical service, including soft-deleted ones.
     */
    @Transactional(readOnly = true)
    public List<SpecificMedicalServiceResponse> listAllSpecificMedicalServices() {
        return specificMedicalServiceRepository.findAll()
                .stream()
                .map(packageServiceMapper::toSpecificMedicalServiceResponse)
                .toList();
    }

    /**
     * Create a new specific medical service bound to an existing medical service.
     */
    public SpecificMedicalServiceResponse createSpecificMedicalService(SpecificMedicalServiceRequest request) {
        
        ensureSpecificSlugAvailable(request.slug(), null);
        SpecificMedicalService entity = packageServiceMapper.toSpecificMedicalService(request);
        entity.setDeleted(false);
        SpecificMedicalService saved = specificMedicalServiceRepository.save(entity);
        return packageServiceMapper.toSpecificMedicalServiceResponse(saved);
    }

    /**
     * Update details of a specific medical service, including slug validation.
     */
    public SpecificMedicalServiceResponse updateSpecificMedicalService(UUID id, SpecificMedicalServiceRequest request) {
        // requireAdmin();
        SpecificMedicalService entity = requireSpecificMedicalService(id);
        if (StringUtils.hasText(request.slug()) && !request.slug().equals(entity.getSlug())) {
            ensureSpecificSlugAvailable(request.slug(), id);
        }
        packageServiceMapper.updateSpecificMedicalServiceFromRequest(request, entity);
        return packageServiceMapper.toSpecificMedicalServiceResponse(entity);
    }

    /**
     * Soft delete a specific medical service so it is no longer selectable.
     */
    public void deleteSpecificMedicalService(UUID id) {
        // requireAdmin();
        SpecificMedicalService entity = requireSpecificMedicalService(id);
        entity.setDeleted(true);
    }

    /**
     * Load a non-deleted medical service or raise a not-found error.
     */
    private MedicalService requireMedicalService(UUID id) {
        return medicalServiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND));
    }

    /**
     * Load a non-deleted specific medical service or raise a not-found error.
     */
    private SpecificMedicalService requireSpecificMedicalService(UUID id) {
        return specificMedicalServiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SPECIFIC_MEDICAL_SERVICE_NOT_FOUND));
    }

    /**
     * Guarantee that the provided medical-service slug is present and unused.
     */
    private void ensureMedicalSlugAvailable(String slug, UUID ignoreId) {
        if (!StringUtils.hasText(slug)) {
            throw new ApiException(ErrorCode.INVALID_SLUG);
        }
        medicalServiceRepository.findBySlug(slug)
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> ignoreId == null || !existing.getId().equals(ignoreId))
                .ifPresent(existing -> {
                    log.warn("Slug conflict detected for medical service '{}'", slug);
                    throw new ApiException(
                        ErrorCode.SLUG_CONFLICT,
                        "Medical service slug '%s' already exists".formatted(slug)
                    );
                });
    
    }

    /**
     * Guarantee that the provided specific medical-service slug is present and unused.
     */
    private void ensureSpecificSlugAvailable(String slug, UUID ignoreId) {
        if (!StringUtils.hasText(slug)) {
            throw new ApiException(ErrorCode.INVALID_SLUG);
        }
        specificMedicalServiceRepository.findBySlug(slug)
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> ignoreId == null || !existing.getId().equals(ignoreId))
                .ifPresent(existing -> {
                    throw new ApiException(ErrorCode.SLUG_CONFLICT);
                });
    }

    /**
     * Ensure the current principal has the ADMIN role.
     */
    // private void requireAdmin() {
    //     currentUserService.requireCurrentUserIdWithRole("ADMIN");
    // }
}
