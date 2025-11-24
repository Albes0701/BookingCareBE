package com.bookingcare.clinic.service;

import com.bookingcare.clinic.client.ExpertiseClient;
import com.bookingcare.clinic.dto.ClinicBranchDoctorDTO;
import com.bookingcare.clinic.dto.DoctorsResponseDTO;
import com.bookingcare.clinic.dto.ClinicBranchRequestDTO;
import com.bookingcare.clinic.dto.ClinicBranchResponseDTO;
import com.bookingcare.clinic.dto.ClinicPackageResponse;
import com.bookingcare.clinic.dto.ClinicPatchRequestDTO;
import com.bookingcare.clinic.dto.ClinicRequestDTO;
import com.bookingcare.clinic.dto.ClinicResponseDTO;
import com.bookingcare.clinic.dto.ClinicRejectionRequestDTO;
import com.bookingcare.clinic.entity.Clinic;
import com.bookingcare.clinic.entity.ClinicBranch;
import com.bookingcare.clinic.entity.ClinicBranchDoctor;
import com.bookingcare.clinic.entity.ClinicStatus;
import com.bookingcare.clinic.entity.ClinicVerification;
import com.bookingcare.clinic.entity.ClinicVerificationAction;
import com.bookingcare.clinic.exception.ApiException;
import com.bookingcare.clinic.exception.ErrorCode;
import com.bookingcare.clinic.mapper.ClinicMapper;
import com.bookingcare.clinic.repository.ClinicBranchDoctorRepository;
import com.bookingcare.clinic.repository.ClinicBranchHealthcheckPackageRepository;
import com.bookingcare.clinic.repository.ClinicBranchRepository;
import com.bookingcare.clinic.repository.ClinicRepository;
import com.bookingcare.clinic.repository.ClinicVerificationRepository;
import com.bookingcare.clinic.repository.ClinicAccountRepository;
import com.bookingcare.clinic.security.CurrentUserService;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import feign.FeignException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ClinicService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ClinicRepository clinicRepository;
    private final ClinicMapper clinicMapper;
    private final CurrentUserService currentUserService;
    private final ClinicBranchRepository clinicBranchRepository;
    private final ClinicBranchDoctorRepository clinicBranchDoctorRepository;
    private final ClinicVerificationRepository clinicVerificationRepository;
    private final ExpertiseClient expertiseClient;
    private final ClinicBranchHealthcheckPackageRepository clinicBranchHealthcheckPackageRepository;
    private final ClinicAccountRepository clinicAccountRepository;

    public ClinicService(ClinicRepository clinicRepository,
                         ClinicMapper clinicMapper,
                         CurrentUserService currentUserService,
                         ClinicBranchRepository clinicBranchRepository,
                         ClinicBranchDoctorRepository clinicBranchDoctorRepository,
                         ClinicVerificationRepository clinicVerificationRepository,
                         ExpertiseClient expertiseClient,
                         ClinicBranchHealthcheckPackageRepository clinicBranchHealthcheckPackageRepository,
                         ClinicAccountRepository clinicAccountRepository) {
        this.clinicRepository = clinicRepository;
        this.clinicMapper = clinicMapper;
        this.currentUserService = currentUserService;
        this.clinicBranchRepository = clinicBranchRepository;
        this.clinicBranchDoctorRepository = clinicBranchDoctorRepository;
        this.clinicVerificationRepository = clinicVerificationRepository;
        this.expertiseClient = expertiseClient;
        this.clinicBranchHealthcheckPackageRepository = clinicBranchHealthcheckPackageRepository;
        this.clinicAccountRepository = clinicAccountRepository;
    }

    // Returns all approved clinics matching optional search criteria.
    @Transactional(readOnly = true)
    public Page<ClinicResponseDTO> getApprovedClinics(String query, Pageable pageable) {
        Pageable effectivePageable = pageable == null ? Pageable.unpaged() : pageable;
        String normalizedQuery = normalizeQuery(query);
        return clinicRepository
                .findByStatusAndSearch(ClinicStatus.APPROVED, normalizedQuery, effectivePageable)
                .map(clinicMapper::toClinicResponseDTO);
    }

    // Lists clinics currently in the pending review state (admin only).
    @Transactional(readOnly = true)
    public Page<ClinicResponseDTO> getPendingClinics(String query, Pageable pageable) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");
        Pageable boundedPageable = applyPageSizeCap(pageable);
        String normalizedQuery = normalizeQuery(query);
        return clinicRepository
                .findByStatusAndSearch(ClinicStatus.PENDING, normalizedQuery, boundedPageable)
                .map(clinicMapper::toClinicResponseDTO);
    }

    // Fetches a specific clinic for admin users regardless of status.
    @Transactional(readOnly = true)
    public ClinicResponseDTO getClinicDetailForAdmin(String clinicId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");
        return clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .map(clinicMapper::toClinicResponseDTO)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));
    }

    // Moves a pending clinic into the approved state.
    @Transactional
    public ClinicResponseDTO approveClinic(String clinicId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");
        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (clinic.getStatus() != ClinicStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION);
        }

        clinic.setStatus(ClinicStatus.APPROVED);
        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Rejects a pending clinic and records the decision metadata.
    @Transactional
    public ClinicResponseDTO rejectClinic(String clinicId, ClinicRejectionRequestDTO request) {
        String adminId = currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");
        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (clinic.getStatus() != ClinicStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION);
        }

        clinic.setStatus(ClinicStatus.REJECTED);
        Clinic savedClinic = clinicRepository.save(clinic);

        ClinicVerification verification = new ClinicVerification();
        verification.setClinic(clinic);
        verification.setAction(ClinicVerificationAction.REJECT);
        verification.setActorId(resolveActorId(adminId));
        verification.setComment(request.reason());
        verification.setDeleted(false);
        clinicVerificationRepository.save(verification);

        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Soft deletes a clinic (admin only) without removing persistence records.
    @Transactional
    public ClinicResponseDTO archiveClinic(String clinicId) {
        String adminId = currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        Clinic clinic = clinicRepository
                .findById(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (clinic.isDeleted()) {
            return clinicMapper.toClinicResponseDTO(clinic);
        }

        clinic.setDeleted(true);
        Clinic savedClinic = clinicRepository.save(clinic);

        ClinicVerification verification = new ClinicVerification();
        verification.setClinic(clinic);
        verification.setAction(ClinicVerificationAction.ARCHIVE);
        verification.setActorId(resolveActorId(adminId));
        verification.setComment("Archived by admin");
        verification.setDeleted(false);
        clinicVerificationRepository.save(verification);

        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Restores a previously soft-deleted clinic.
    @Transactional
    public ClinicResponseDTO restoreClinic(String clinicId) {
        String adminId = currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        Clinic clinic = clinicRepository
                .findById(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!clinic.isDeleted()) {
            return clinicMapper.toClinicResponseDTO(clinic);
        }

        clinic.setDeleted(false);
        Clinic savedClinic = clinicRepository.save(clinic);

        ClinicVerification verification = new ClinicVerification();
        verification.setClinic(clinic);
        verification.setAction(ClinicVerificationAction.RESTORE);
        verification.setActorId(resolveActorId(adminId));
        verification.setComment("Restored by admin");
        verification.setDeleted(false);
        clinicVerificationRepository.save(verification);

        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Converts a string userId into a UUID (deterministic fallback when needed).
    private UUID resolveActorId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(userId.getBytes(StandardCharsets.UTF_8));
        }
    }

    // Supplies the public-facing details for a single approved clinic.
    @Transactional(readOnly = true)
    public ClinicResponseDTO getApprovedClinicDetail(String clinicId) {
        return clinicRepository
                .findByIdAndStatusAndIsDeletedFalse(clinicId, ClinicStatus.APPROVED)
                .map(clinicMapper::toClinicResponseDTO)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));
    }

    // Resolves clinic detail either for an owner (any status) or public (approved only).
    @Transactional(readOnly = true)
    public ClinicResponseDTO getClinicDetail(String clinicId) {
        Optional<String> doctorUserId = currentUserService.findCurrentUserIdWithRole("ROLE_DOCTOR");

        if (doctorUserId.isPresent()) {
            Clinic clinic = clinicRepository
                    .findByIdAndIsDeletedFalse(clinicId)
                    .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

            if (!doctorUserId.get().equals(clinic.getCreatedByUserId())) {
                throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
            }
            return clinicMapper.toClinicResponseDTO(clinic);
        }

        return getApprovedClinicDetail(clinicId);
    }

    // Returns branch listings, allowing owners to view their clinic branches.
    @Transactional(readOnly = true)
    public List<ClinicBranchResponseDTO> getClinicBranches(String clinicId) {
        Optional<String> doctorUserId = currentUserService.findCurrentUserIdWithRole("ROLE_DOCTOR");

        if (doctorUserId.isPresent()) {
            Clinic clinic = clinicRepository
                    .findByIdAndIsDeletedFalse(clinicId)
                    .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

            if (!doctorUserId.get().equals(clinic.getCreatedByUserId())) {
                throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
            }
        } else {
            clinicRepository
                    .findByIdAndStatusAndIsDeletedFalse(clinicId, ClinicStatus.APPROVED)
                    .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));
        }

        return clinicBranchRepository.findByClinic_IdAndIsDeletedFalse(clinicId)
                .stream()
                .map(clinicMapper::toClinicBranchResponseDTO)
                .collect(Collectors.toList());
    }

    // Admin listing of all branches for a clinic (including soft-deleted records).
    @Transactional(readOnly = true)
    public List<ClinicBranchResponseDTO> getClinicBranchesForAdmin(String clinicId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        return clinicBranchRepository.findByClinic_Id(clinicId)
                .stream()
                .map(clinicMapper::toClinicBranchResponseDTO)
                .collect(Collectors.toList());
    }

    // Admin lookup for doctor assignments scoped to a branch.
    @Transactional(readOnly = true)
    public List<ClinicBranchDoctorDTO> getClinicBranchDoctorsForAdmin(String branchId,
                                                                       Boolean isDeleted) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        clinicBranchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        List<ClinicBranchDoctor> branchDoctors = (isDeleted == null)
                ? clinicBranchDoctorRepository.findByClinicBranch_Id(branchId)
                : clinicBranchDoctorRepository.findByClinicBranch_IdAndIsDeleted(branchId, isDeleted);

        return branchDoctors.stream()
                .map(clinicMapper::toClinicBranchDoctorDTO)
                .collect(Collectors.toList());
    }

    // Admin action that attaches (or reactivates) a doctor with the target branch.
    @Transactional
    public ClinicBranchDoctorDTO assignDoctorToClinicBranch(String branchId,
                                                            String doctorUserId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        ClinicBranch branch = clinicBranchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        DoctorsResponseDTO doctor;
        try {
            doctor = expertiseClient.getDoctorByUserId(doctorUserId);
        } catch (FeignException.NotFound ex) {
            throw new ApiException(ErrorCode.DOCTOR_NOT_FOUND);
        } catch (FeignException ex) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if (doctor == null || doctor.isDeleted()) {
            throw new ApiException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        ClinicBranchDoctor branchDoctor = clinicBranchDoctorRepository
                .findByClinicBranch_IdAndDoctorId(branchId, doctor.id())
                .orElseGet(() -> {
                    ClinicBranchDoctor newAssignment = new ClinicBranchDoctor();
                    newAssignment.setClinicBranch(branch);
                    newAssignment.setDoctorId(doctor.id());
                    newAssignment.setDeleted(false);
                    return newAssignment;
                });

        if (branchDoctor.isDeleted()) {
            branchDoctor.setDeleted(false);
        }

        ClinicBranchDoctor saved = clinicBranchDoctorRepository.save(branchDoctor);
        return clinicMapper.toClinicBranchDoctorDTO(saved);
    }

    // Admin action that soft deletes the doctor assignment.
    @Transactional
    public ClinicBranchDoctorDTO archiveClinicBranchDoctor(String branchId,
                                                           String doctorId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        clinicBranchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        ClinicBranchDoctor branchDoctor = clinicBranchDoctorRepository
                .findByClinicBranch_IdAndDoctorId(branchId, doctorId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        if (branchDoctor.isDeleted()) {
            return clinicMapper.toClinicBranchDoctorDTO(branchDoctor);
        }

        branchDoctor.setDeleted(true);
        ClinicBranchDoctor saved = clinicBranchDoctorRepository.save(branchDoctor);
        return clinicMapper.toClinicBranchDoctorDTO(saved);
    }

    // Admin action that restores a soft-deleted doctor assignment.
    @Transactional
    public ClinicBranchDoctorDTO restoreClinicBranchDoctor(String branchId,
                                                           String doctorId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        clinicBranchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        ClinicBranchDoctor branchDoctor = clinicBranchDoctorRepository
                .findByClinicBranch_IdAndDoctorId(branchId, doctorId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!branchDoctor.isDeleted()) {
            return clinicMapper.toClinicBranchDoctorDTO(branchDoctor);
        }

        branchDoctor.setDeleted(false);
        ClinicBranchDoctor saved = clinicBranchDoctorRepository.save(branchDoctor);
        return clinicMapper.toClinicBranchDoctorDTO(saved);
    }

    // Soft deletes a branch and tracks the action for auditing.
    @Transactional
    public ClinicBranchResponseDTO archiveClinicBranch(String clinicId, String branchId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        ClinicBranch branch = clinicBranchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        if (branch.isDeleted()) {
            return clinicMapper.toClinicBranchResponseDTO(branch);
        }

        branch.setDeleted(true);
        ClinicBranch savedBranch = clinicBranchRepository.save(branch);
        return clinicMapper.toClinicBranchResponseDTO(savedBranch);
    }

    // Restores a soft-deleted branch under the specified clinic.
    @Transactional
    public ClinicBranchResponseDTO restoreClinicBranch(String clinicId, String branchId) {
        currentUserService.requireCurrentUserIdWithRole("ROLE_ADMIN");

        ClinicBranch branch = clinicBranchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        if (!branch.isDeleted()) {
            return clinicMapper.toClinicBranchResponseDTO(branch);
        }

        branch.setDeleted(false);
        ClinicBranch savedBranch = clinicBranchRepository.save(branch);
        return clinicMapper.toClinicBranchResponseDTO(savedBranch);
    }

    // Creates a new branch for the doctor's clinic.
    @Transactional
    public ClinicBranchResponseDTO createClinicBranch(String clinicId, ClinicBranchRequestDTO request) {
        String doctorUserId = currentUserService.requireCurrentUserIdWithRole("ROLE_DOCTOR");

        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!doctorUserId.equals(clinic.getCreatedByUserId())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
        }

        return clinicMapper.toClinicBranchResponseDTO(
                clinicBranchRepository.save(clinicMapper.toClinicBranchEntity(clinic, request))
        );
    }

    // Updates branch metadata belonging to the doctor's clinic.
    @Transactional
    public ClinicBranchResponseDTO updateClinicBranch(String clinicId, String branchId, ClinicBranchRequestDTO request) {
        String doctorUserId = currentUserService.requireCurrentUserIdWithRole("ROLE_DOCTOR");

        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!doctorUserId.equals(clinic.getCreatedByUserId())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
        }

        ClinicBranch branch = clinicBranchRepository
                .findByIdAndClinic_IdAndIsDeletedFalse(branchId, clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        clinicMapper.updateClinicBranchFromRequest(request, branch);
        ClinicBranch savedBranch = clinicBranchRepository.save(branch);

        return clinicMapper.toClinicBranchResponseDTO(savedBranch);
    }

    // Hard deletes a branch owned by the doctor when allowed.
    @Transactional
    public void deleteClinicBranch(String clinicId, String branchId) {
        String doctorUserId = currentUserService.requireCurrentUserIdWithRole("ROLE_DOCTOR");

        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!doctorUserId.equals(clinic.getCreatedByUserId())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
        }

        ClinicBranch branch = clinicBranchRepository
                .findByIdAndClinic_IdAndIsDeletedFalse(branchId, clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));

        clinicBranchRepository.delete(branch);
    }

    // Public endpoint helper that returns only non-deleted branches for approved clinics.
    @Transactional(readOnly = true)
    public ClinicBranchResponseDTO getApprovedClinicBranchDetail(String clinicId, String branchId) {
        clinicRepository
                .findByIdAndStatusAndIsDeletedFalse(clinicId, ClinicStatus.APPROVED)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        return clinicBranchRepository
                .findByIdAndClinic_IdAndIsDeletedFalse(branchId, clinicId)
                .map(clinicMapper::toClinicBranchResponseDTO)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_BRANCH_NOT_FOUND));
    }

    @Transactional
    // Creates a new clinic owned by the current authenticated doctor.
    public ClinicResponseDTO createClinic(ClinicRequestDTO request) {
        String currentUserId = currentUserService.requireCurrentUserId();
        Clinic clinic = clinicMapper.toClinicEntity(request);
        clinic.setStatus(ClinicStatus.DRAFT);
        clinic.setCreatedByUserId(currentUserId);
        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Updates mutable fields of a draft/rejected clinic owned by the doctor.
    @Transactional
    public ClinicResponseDTO patchClinic(String clinicId, ClinicPatchRequestDTO request) {
        String currentUserId = currentUserService.requireCurrentUserId();

        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!currentUserId.equals(clinic.getCreatedByUserId())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
        }

        if (!EnumSet.of(ClinicStatus.DRAFT, ClinicStatus.REJECTED).contains(clinic.getStatus())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_EDITABLE);
        }

        clinicMapper.patchClinicFromRequest(request, clinic);

        if (!StringUtils.hasText(clinic.getName())) {
            throw new ApiException(ErrorCode.CLINIC_NAME_REQUIRED);
        }

        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Submits a clinic for admin review (transitions to the pending state).
    @Transactional
    public ClinicResponseDTO submitClinic(String clinicId) {
        String currentUserId = currentUserService.requireCurrentUserId();

        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!currentUserId.equals(clinic.getCreatedByUserId())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
        }

        if (!EnumSet.of(ClinicStatus.DRAFT, ClinicStatus.REJECTED).contains(clinic.getStatus())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_EDITABLE);
        }

        clinic.setStatus(ClinicStatus.PENDING);

        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponseDTO(savedClinic);
    }

    // Performs a hard delete of a clinic owned by the doctor when allowed.
    @Transactional
    public void deleteClinic(String clinicId) {
        String currentUserId = currentUserService.requireCurrentUserId();

        Clinic clinic = clinicRepository
                .findByIdAndIsDeletedFalse(clinicId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));

        if (!currentUserId.equals(clinic.getCreatedByUserId())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_OWNED);
        }

        if (!EnumSet.of(ClinicStatus.DRAFT, ClinicStatus.REJECTED).contains(clinic.getStatus())) {
            throw new ApiException(ErrorCode.CLINIC_NOT_EDITABLE);
        }

        clinicRepository.delete(clinic);
    }

    // Lists clinics owned by the current doctor using optional filters.
    @Transactional(readOnly = true)
    public Page<ClinicResponseDTO> getMyClinics(String statusValue, String query, Pageable pageable) {
        String currentUserId = currentUserService.requireCurrentUserId();
        ClinicStatus statusFilter = resolveStatus(statusValue);
        Pageable boundedPageable = applyPageSizeCap(pageable);
        String normalizedQuery = normalizeQuery(query);

        return clinicRepository
                .findByOwnerAndFilters(currentUserId, statusFilter, normalizedQuery, boundedPageable)
                .map(clinicMapper::toClinicResponseDTO);
    }

    // Ensures pageable size is bounded to avoid excessive queries.
    private Pageable applyPageSizeCap(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, Math.min(20, MAX_PAGE_SIZE));
        }
        int boundedSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        if (boundedSize == pageable.getPageSize()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), boundedSize, pageable.getSort());
    }

    // Normalizes free-text search input (trim + empty to null).
    private String normalizeQuery(String query) {
        return (query == null || query.isBlank()) ? null : query.trim();
    }

    // Converts a user-provided status filter into an enum or throws.
    private ClinicStatus resolveStatus(String statusValue) {
        if (!StringUtils.hasText(statusValue)) {
            return null;
        }
        try {
            return ClinicStatus.valueOf(statusValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.INVALID_STATUS_FILTER);
        }
    }


    public ClinicResponseDTO getApprovedClinicDetailBySlug(String clinicSlug) { 
        return clinicRepository
                .findBySlugAndStatusAndIsDeletedFalse(clinicSlug, ClinicStatus.APPROVED)
                .map(clinicMapper::toClinicResponseDTO)
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));
    }


    public List<ClinicPackageResponse> getPackageByClinicBranchId(String clinicBranchId) {
        return clinicBranchHealthcheckPackageRepository.findPackageIdByClinicBranchId(clinicBranchId)
                .stream()
                .collect(Collectors.toList());
    }

    // Get clinic info for authenticated clinic admin user
    // Extracts accountId from JWT token and finds associated clinic
    @Transactional(readOnly = true)
    public ClinicResponseDTO getClinicForAdmin() {
        // Get current user's account ID from JWT token
        String accountId = currentUserService.requireCurrentUserId();
        
        // Find clinic account mapping
        var clinicAccount = clinicAccountRepository.findByAccountIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found for account: " + accountId
                ));
        
        // Get clinic by clinicId from mapping
        var clinic = clinicRepository.findById(clinicAccount.getClinicId())
                .orElseThrow(() -> new ApiException(ErrorCode.CLINIC_NOT_FOUND));
        
        // Map to response DTO
        return clinicMapper.toClinicResponseDTO(clinic);
    }

}
