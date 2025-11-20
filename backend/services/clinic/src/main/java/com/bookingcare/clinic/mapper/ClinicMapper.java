package com.bookingcare.clinic.mapper;

import com.bookingcare.clinic.dto.ClinicBranchDoctorDTO;
import com.bookingcare.clinic.dto.ClinicBranchHealthcheckPackageDTO;
import com.bookingcare.clinic.dto.ClinicBranchRequestDTO;
import com.bookingcare.clinic.dto.ClinicBranchResponseDTO;
import com.bookingcare.clinic.dto.ClinicPatchRequestDTO;
import com.bookingcare.clinic.dto.ClinicRequestDTO;
import com.bookingcare.clinic.dto.ClinicResponseDTO;
import com.bookingcare.clinic.dto.ClinicVerificationRequestDTO;
import com.bookingcare.clinic.dto.ClinicVerificationResponseDTO;
import com.bookingcare.clinic.entity.Clinic;
import com.bookingcare.clinic.entity.ClinicBranch;
import com.bookingcare.clinic.entity.ClinicBranchDoctor;
import com.bookingcare.clinic.entity.ClinicBranchHealthcheckPackage;
import com.bookingcare.clinic.entity.ClinicStatus;
import com.bookingcare.clinic.entity.ClinicVerification;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ClinicMapper {

    // Maps a Clinic entity into its external response representation.
    public ClinicResponseDTO toClinicResponseDTO(Clinic clinic) {
        if (clinic == null) {
            return null;
        }

        return new ClinicResponseDTO(
                clinic.getId(),
                clinic.getFullname(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getClinicDetailInfo(),
                clinic.getImage(),
                clinic.getSlug(),
                clinic.getStatus(),
                clinic.isDeleted()
        );
    }

    // Constructs a Clinic entity from a create/update request.
    public Clinic toClinicEntity(ClinicRequestDTO request) {
        if (request == null) {
            return null;
        }

        Clinic clinic = new Clinic();
        fillClinicFromRequest(request, clinic);
        clinic.setDeleted(false);
        return clinic;
    }

    // Copies mutable fields from the request onto an existing Clinic entity.
    public void updateClinicFromRequest(ClinicRequestDTO request, Clinic clinic) {
        if (request == null || clinic == null) {
            return;
        }
        fillClinicFromRequest(request, clinic);
    }

    // Applies only non-null patch fields onto the existing Clinic entity.
    public void patchClinicFromRequest(ClinicPatchRequestDTO request, Clinic clinic) {
        if (request == null || clinic == null) {
            return;
        }

        if (request.fullname() != null) {
            clinic.setFullname(request.fullname());
        }
        if (request.name() != null) {
            clinic.setName(request.name());
        }
        if (request.address() != null) {
            clinic.setAddress(request.address());
        }
        if (request.clinicDetailInfo() != null) {
            clinic.setClinicDetailInfo(request.clinicDetailInfo());
        }
        if (request.image() != null) {
            clinic.setImage(request.image());
        }
        if (request.slug() != null) {
            clinic.setSlug(resolveSlug(request.slug(), clinic.getName()));
        }
    }

    // Shared mapping logic for Clinic mutations
    private void fillClinicFromRequest(ClinicRequestDTO request, Clinic clinic) {
        clinic.setFullname(request.fullname());
        clinic.setName(request.name());
        clinic.setAddress(request.address());
        clinic.setClinicDetailInfo(request.clinicDetailInfo());
        clinic.setImage(request.image());
        clinic.setSlug(resolveSlug(request.slug(), request.name()));
        clinic.setStatus(request.status() != null ? request.status() : ClinicStatus.DRAFT);
    }

    // Maps a ClinicBranch entity into its REST response DTO.
    public ClinicBranchResponseDTO toClinicBranchResponseDTO(ClinicBranch branch) {
        if (branch == null) {
            return null;
        }

        return new ClinicBranchResponseDTO(
                branch.getId(),
                branch.getClinic() != null ? branch.getClinic().getId() : null,
                branch.getName(),
                branch.getAddress(),
                branch.isDeleted()
        );
    }

    // Builds a ClinicBranch entity for creation using the owning Clinic.
    public ClinicBranch toClinicBranchEntity(Clinic clinic, ClinicBranchRequestDTO request) {
        if (clinic == null || request == null) {
            return null;
        }

        ClinicBranch branch = new ClinicBranch();
        branch.setClinic(clinic);
        branch.setName(request.name());
        branch.setAddress(request.address());
        return branch;
    }

    // Updates the branch entity with the latest name/address from the request.
    public void updateClinicBranchFromRequest(ClinicBranchRequestDTO request, ClinicBranch branch) {
        if (request == null || branch == null) {
            return;
        }
        branch.setName(request.name());
        branch.setAddress(request.address());
    }

    // Converts a verification entity into the DTO returned by the API.
    public ClinicVerificationResponseDTO toClinicVerificationResponseDTO(ClinicVerification verification) {
        if (verification == null) {
            return null;
        }

        return new ClinicVerificationResponseDTO(
                verification.getId(),
                verification.getClinic() != null ? verification.getClinic().getId() : null,
                verification.getAction(),
                verification.getActorId(),
                verification.getComment(),
                verification.getCreatedAt(),
                verification.isDeleted()
        );
    }

    // Instantiates a ClinicVerification entity from request data.
    public ClinicVerification toClinicVerificationEntity(ClinicVerificationRequestDTO request) {
        if (request == null) {
            return null;
        }

        ClinicVerification verification = new ClinicVerification();
        fillClinicVerificationFromRequest(request, verification);
        return verification;
    }

    // Applies mutable verification fields from the request onto the entity.
    public void updateClinicVerificationFromRequest(ClinicVerificationRequestDTO request, ClinicVerification verification) {
        if (request == null || verification == null) {
            return;
        }
        fillClinicVerificationFromRequest(request, verification);
    }

    // Shared mapping logic for ClinicVerification mutations
    private void fillClinicVerificationFromRequest(ClinicVerificationRequestDTO request, ClinicVerification verification) {
        verification.setClinic(referenceClinic(request.clinicId()));
        verification.setAction(request.action());
        verification.setActorId(request.actorId());
        verification.setComment(request.comment());
        if (request.isDeleted() != null) {
            verification.setDeleted(request.isDeleted());
        }
    }

    // Maps branch-doctor assignments to the DTO exposed via REST.
    public ClinicBranchDoctorDTO toClinicBranchDoctorDTO(ClinicBranchDoctor branchDoctor) {
        if (branchDoctor == null) {
            return null;
        }

        return new ClinicBranchDoctorDTO(
                branchDoctor.getId(),
                branchDoctor.getClinicBranch() != null ? branchDoctor.getClinicBranch().getId() : null,
                branchDoctor.getDoctorId(),
                branchDoctor.isDeleted()
        );
    }

    // Maps branch-healthcheck package assignments to their DTO form.
    public ClinicBranchHealthcheckPackageDTO toClinicBranchHealthcheckPackageDTO(ClinicBranchHealthcheckPackage branchPackage) {
        if (branchPackage == null) {
            return null;
        }

        return new ClinicBranchHealthcheckPackageDTO(
                branchPackage.getId(),
                branchPackage.getClinicBranch() != null ? branchPackage.getClinicBranch().getId() : null,
                branchPackage.getHealthcheckPackageId(),
                branchPackage.isDeleted()
        );
    }

    // Lightweight reference builder for Clinic association fields
    private Clinic referenceClinic(String clinicId) {
        if (clinicId == null || clinicId.isBlank()) {
            return null;
        }
        Clinic clinic = new Clinic();
        clinic.setId(clinicId.trim());
        return clinic;
    }

    // Fallback slug generator that converts names to URL friendly identifiers
    private String resolveSlug(String suppliedSlug, String fallbackName) {
        if (suppliedSlug != null && !suppliedSlug.isBlank()) {
            return suppliedSlug.trim();
        }
        if (fallbackName == null || fallbackName.isBlank()) {
            return null;
        }
        String slug = fallbackName.trim().toLowerCase(Locale.ROOT);
        slug = slug.replaceAll("[^\\p{Alnum}]+", "-");
        slug = slug.replaceAll("(^-+|-+$)", "");
        return slug.isBlank() ? null : slug;
    }
}
