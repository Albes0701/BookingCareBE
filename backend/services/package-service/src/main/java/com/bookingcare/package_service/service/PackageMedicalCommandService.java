package com.bookingcare.package_service.service;

import com.bookingcare.package_service.client.ExpertiseClient;
import com.bookingcare.package_service.dto.DoctorsResponseDTO;
import com.bookingcare.package_service.dto.HealthCheckPackageRequest;
import com.bookingcare.package_service.dto.HealthCheckPackageResponse;
import com.bookingcare.package_service.dto.HealthCheckPackageSpecialtyLinkRequest;
import com.bookingcare.package_service.dto.HealthCheckPackageSpecialtyResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceHealthCheckPackageResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceLinkRequest;
import com.bookingcare.package_service.entity.ApprovalStatus;
import com.bookingcare.package_service.entity.HealthCheckPackage;
import com.bookingcare.package_service.entity.HealthCheckPackageSpecialty;
import com.bookingcare.package_service.entity.HealthCheckPackageSpecialtyId;
import com.bookingcare.package_service.entity.PackageType;
import com.bookingcare.package_service.entity.SpecificMedicalService;
import com.bookingcare.package_service.entity.SpecificMedicalServiceHealthCheckPackage;
import com.bookingcare.package_service.entity.SpecificMedicalServiceHealthCheckPackageId;
import com.bookingcare.package_service.exception.ApiException;
import com.bookingcare.package_service.exception.ErrorCode;
import com.bookingcare.package_service.mapper.PackageServiceMapper;
import com.bookingcare.package_service.repository.HealthCheckPackageRepository;
import com.bookingcare.package_service.repository.HealthCheckPackageSpecialtyRepository;
import com.bookingcare.package_service.repository.PackageTypeRepository;
import com.bookingcare.package_service.repository.SpecificMedicalServiceHealthCheckPackageRepository;
import com.bookingcare.package_service.repository.SpecificMedicalServiceRepository;
import com.bookingcare.package_service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PackageMedicalCommandService {

    private final HealthCheckPackageRepository healthCheckPackageRepository;
    private final PackageTypeRepository packageTypeRepository;
    private final HealthCheckPackageSpecialtyRepository healthCheckPackageSpecialtyRepository;
    private final SpecificMedicalServiceRepository specificMedicalServiceRepository;
    private final SpecificMedicalServiceHealthCheckPackageRepository specificMedicalServiceHealthCheckPackageRepository;
    private final PackageServiceMapper packageServiceMapper;
    private final CurrentUserService currentUserService;
    private final ExpertiseClient expertiseClient;

    /**
     * Create a draft health-check package owned by the current doctor.
     */
    public HealthCheckPackageResponse createService(HealthCheckPackageRequest request) {
        String userId = currentUserService.requireCurrentUserId();
        DoctorsResponseDTO doctor = expertiseClient.getDoctorByUserId(userId);
        ensureSlugAvailable(request.slug(), null);

        HealthCheckPackage entity = packageServiceMapper.toHealthCheckPackage(request);
        entity.setManagedByDoctor(true);
        entity.setManagingDoctorId(doctor.id());
        entity.setStatus(ApprovalStatus.DRAFT);
        entity.setRejectedReason(null);
        entity.setSubmittedAt(null);
        entity.setApprovedAt(null);
        entity.setDeleted(false);
        entity.setPackageType(resolvePackageType(request.packageTypeId()));

        HealthCheckPackage saved = healthCheckPackageRepository.save(entity);
        return packageServiceMapper.toHealthCheckPackageResponse(saved);
    }

    /**
     * List every non-deleted package managed by the current doctor.
     */
    @Transactional(readOnly = true)
    public List<HealthCheckPackageResponse> getMyPackages() {
        String userId = currentUserService.requireCurrentUserId();

        DoctorsResponseDTO doctor = expertiseClient.getDoctorByUserId(userId);
        if (doctor == null) {
            throw new ApiException(ErrorCode.DOCTOR_NOT_FOUND, "Doctor not found");
        }

        return healthCheckPackageRepository.findAllByManagingDoctorIdAndDeletedFalse(doctor.id())
                .stream()
                .map(packageServiceMapper::toHealthCheckPackageResponse)
                .toList();
    }

    /**
     * Update a draft/rejected package that belongs to the current doctor.
     */
    public HealthCheckPackageResponse updateService(UUID id, HealthCheckPackageRequest request) {
        HealthCheckPackage entity = requireOwnedPackage(id);
        assertDraftOrRejected(entity);

        if (StringUtils.hasText(request.slug())) {
            ensureSlugAvailable(request.slug(), id);
        }
        packageServiceMapper.updateHealthCheckPackageFromRequest(request, entity);
        if (request.packageTypeId() != null) {
            entity.setPackageType(resolvePackageType(request.packageTypeId()));
        }
        entity.setRejectedReason(null); // clear rejected reason on any update
        entity.setName(request.name());
        entity.setImage(request.image());
        entity.setSlug(request.slug());
        entity.setPackageDetailInfo(request.packageDetailInfo());
        entity.setShortPackageInfo(request.shortPackageInfo());
        entity.setSubmittedAt(request.submittedAt());
        entity.setApprovedAt(request.approvedAt());

        HealthCheckPackage saved = healthCheckPackageRepository.save(entity);
        return packageServiceMapper.toHealthCheckPackageResponse(saved);
    }

    /**
     * Submit a draft or rejected package for admin approval.
     */
    public HealthCheckPackageResponse submitService(UUID id) {
        HealthCheckPackage entity = requireOwnedPackage(id);
        assertDraftOrRejected(entity);

        entity.setStatus(ApprovalStatus.PENDING);
        entity.setSubmittedAt(OffsetDateTime.now());
        entity.setApprovedAt(null);
        entity.setRejectedReason(null);

        return packageServiceMapper.toHealthCheckPackageResponse(entity);
    }

    /**
     * Move a pending package back to draft so the doctor can edit it again.
     */
    public HealthCheckPackageResponse unsubmitService(UUID id) {
        HealthCheckPackage entity = requireOwnedPackage(id);
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new ApiException(ErrorCode.PACKAGE_NOT_EDITABLE, "Only pending services can be unsubmitted");
        }

        entity.setStatus(ApprovalStatus.DRAFT);
        entity.setSubmittedAt(null);
        return packageServiceMapper.toHealthCheckPackageResponse(entity);
    }

    /**
     * List every pending package for admins.
     */
    @Transactional(readOnly = true)
    public List<HealthCheckPackageResponse> getPendingPackages() {
        requireAdmin();
        return listByStatus(ApprovalStatus.PENDING);
    }

    /**
     * Alias for getPendingPackages maintained for API symmetry.
     */
    @Transactional(readOnly = true)
    public List<HealthCheckPackageResponse> getPendingServices() {
        requireAdmin();
        return listByStatus(ApprovalStatus.PENDING);
    }

    /**
     * List every rejected package for admins.
     */
    @Transactional(readOnly = true)
    public List<HealthCheckPackageResponse> getRejectedPackages() {
        requireAdmin();
        return listByStatus(ApprovalStatus.REJECTED);
    }

    /**
     * Alias for getRejectedPackages maintained for API symmetry.
     */
    @Transactional(readOnly = true)
    public List<HealthCheckPackageResponse> getRejectedServices() {
        requireAdmin();
        return listByStatus(ApprovalStatus.REJECTED);
    }

    /**
     * Approve a pending package and stamp approval metadata.
     */
    public HealthCheckPackageResponse approvePackage(UUID id) {
        requireAdmin();
        HealthCheckPackage entity = requirePackageById(id);
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATUS_TRANSITION, "Only pending packages can be approved");
        }
        entity.setStatus(ApprovalStatus.APPROVED);
        entity.setApprovedAt(OffsetDateTime.now());
        entity.setRejectedReason(null);
        return packageServiceMapper.toHealthCheckPackageResponse(entity);
    }

    /**
     * Alias for approvePackage maintained for API symmetry.
     */
    public HealthCheckPackageResponse approveService(UUID id) {
        return approvePackage(id);
    }

    /**
     * Reject a pending package with an optional reason.
     */
    public HealthCheckPackageResponse rejectPackage(UUID id, String reason) {
        requireAdmin();
        HealthCheckPackage entity = requirePackageById(id);
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATUS_TRANSITION, "Only pending packages can be rejected");
        }
        entity.setStatus(ApprovalStatus.REJECTED);
        entity.setRejectedReason(StringUtils.hasText(reason) ? reason : "Rejected by admin");
        entity.setApprovedAt(null);
        return packageServiceMapper.toHealthCheckPackageResponse(entity);
    }

    /**
     * Alias for rejectPackage maintained for API symmetry.
     */
    public HealthCheckPackageResponse rejectService(UUID id, String reason) {
        return rejectPackage(id, reason);
    }

    /**
     * Soft delete a doctor-managed package while it is editable.
     */
    public void softDeleteService(UUID id) {
        HealthCheckPackage entity = requireOwnedPackage(id);
        assertDraftOrRejected(entity);
        entity.setDeleted(true);
    }

    /**
     * Link a specific medical service to a package owned by the current doctor.
     */
    public SpecificMedicalServiceHealthCheckPackageResponse addSpecific(UUID packageId, SpecificMedicalServiceLinkRequest request) {
        HealthCheckPackage healthCheckPackage = requireOwnedPackage(packageId);
        assertDraftOrRejected(healthCheckPackage);

        SpecificMedicalService specific = specificMedicalServiceRepository.findByIdAndDeletedFalse(request.specificMedicalServiceId())
                .orElseThrow(() -> new ApiException(ErrorCode.SPECIFIC_MEDICAL_SERVICE_NOT_FOUND));

        SpecificMedicalServiceHealthCheckPackageId linkId = new SpecificMedicalServiceHealthCheckPackageId(
                specific.getId(),
                healthCheckPackage.getId()
        );

        if (specificMedicalServiceHealthCheckPackageRepository.existsById(linkId)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Specific medical service already exists in this service");
        }

        SpecificMedicalServiceHealthCheckPackage link = new SpecificMedicalServiceHealthCheckPackage();
        link.setId(linkId);
        link.setHealthCheckPackage(healthCheckPackage);
        link.setSpecificMedicalService(specific);

        specificMedicalServiceHealthCheckPackageRepository.save(link);
        return new SpecificMedicalServiceHealthCheckPackageResponse(linkId.getSpecificMedicalServiceId(), linkId.getPackageId());
    }

    /**
     * Remove a specific medical service link from a doctor-owned package.
     */
    public void removeSpecific(UUID packageId, UUID specificId) {
        HealthCheckPackage healthCheckPackage = requireOwnedPackage(packageId);
        assertDraftOrRejected(healthCheckPackage);

        SpecificMedicalServiceHealthCheckPackageId linkId = new SpecificMedicalServiceHealthCheckPackageId(specificId, packageId);
        if (!specificMedicalServiceHealthCheckPackageRepository.existsById(linkId)) {
            throw new ApiException(ErrorCode.SPECIFIC_MEDICAL_SERVICE_NOT_FOUND, "Specific medical service is not linked to this service");
        }
        specificMedicalServiceHealthCheckPackageRepository.deleteById(linkId);
    }

    /**
     * Link a specialty to a doctor-owned package while in editable states.
     */
    public HealthCheckPackageSpecialtyResponse addSpecialty(UUID packageId, HealthCheckPackageSpecialtyLinkRequest request) {
        HealthCheckPackage healthCheckPackage = requireOwnedPackage(packageId);
        assertDraftOrRejected(healthCheckPackage);

        if (request.specialtyId() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "specialtyId is required");
        }

        HealthCheckPackageSpecialtyId id = new HealthCheckPackageSpecialtyId(packageId, request.specialtyId());
        if (healthCheckPackageSpecialtyRepository.existsById(id)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Specialty already linked");
        }

        HealthCheckPackageSpecialty link = new HealthCheckPackageSpecialty();
        link.setId(id);
        link.setHealthCheckPackage(healthCheckPackage);
        healthCheckPackageSpecialtyRepository.save(link);

        return new HealthCheckPackageSpecialtyResponse(id.getPackageId(), id.getSpecialtyId());
    }

    /**
     * Remove a specialty link from a doctor-owned package.
     */
    public void removeSpecialty(UUID packageId, UUID specialtyId) {
        HealthCheckPackage healthCheckPackage = requireOwnedPackage(packageId);
        assertDraftOrRejected(healthCheckPackage);

        HealthCheckPackageSpecialtyId id = new HealthCheckPackageSpecialtyId(packageId, specialtyId);
        if (!healthCheckPackageSpecialtyRepository.existsById(id)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Specialty not linked to this package");
        }
        healthCheckPackageSpecialtyRepository.deleteById(id);
    }

    /**
     * Load a package and ensure the current doctor is its owner.
     */
    private HealthCheckPackage requireOwnedPackage(UUID id) {

        String userId = currentUserService.requireCurrentUserId();
        DoctorsResponseDTO doctor = expertiseClient.getDoctorByUserId(userId);
        HealthCheckPackage entity = requirePackageById(id);
        
        if (!doctor.id().equals(entity.getManagingDoctorId())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not own this service");
        }
        return entity;
    }

    /**
     * Load an existing non-deleted package or throw if absent.
     */
    private HealthCheckPackage requirePackageById(UUID id) {
        return healthCheckPackageRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PACKAGE_NOT_FOUND));
    }

    /**
     * List packages for admins filtered by the provided status while skipping deleted ones.
     */
    private List<HealthCheckPackageResponse> listByStatus(ApprovalStatus status) {
        return healthCheckPackageRepository.findAllByStatus(status)
                .stream()
                .filter(pkg -> !pkg.isDeleted())
                .map(packageServiceMapper::toHealthCheckPackageResponse)
                .toList();
    }

    /**
     * Ensure the caller is an administrator.
     */
    private void requireAdmin() {
        currentUserService.requireCurrentUserIdWithRole("ADMIN");
    }

    /**
     * Guard that a package is still editable (draft or rejected).
     */
    private void assertDraftOrRejected(HealthCheckPackage entity) {
        ApprovalStatus status = entity.getStatus();
        if (status != ApprovalStatus.DRAFT && status != ApprovalStatus.REJECTED) {
            throw new ApiException(ErrorCode.PACKAGE_NOT_EDITABLE);
        }
    }

    /**
     * Validate that a slug is present and unique, ignoring the provided id if set.
     */
    private void ensureSlugAvailable(String slug, UUID ignoreId) {
        if (!StringUtils.hasText(slug)) {
            throw new ApiException(ErrorCode.INVALID_SLUG);
        }
        healthCheckPackageRepository.findBySlugAndDeletedFalse(slug)
                .filter(existing -> ignoreId == null || !existing.getId().equals(ignoreId))
                .ifPresent(existing -> {
                    throw new ApiException(ErrorCode.SLUG_CONFLICT);
                });
    }

    /**
     * Resolve and return the package type referenced in a request.
     */
    private PackageType resolvePackageType(UUID packageTypeId) {
        if (packageTypeId == null) {
            throw new ApiException(ErrorCode.PACKAGE_TYPE_REQUIRED);
        }
        return packageTypeRepository.findById(packageTypeId)
                .orElseThrow(() -> new ApiException(ErrorCode.PACKAGE_TYPE_NOT_FOUND));
    }
}
