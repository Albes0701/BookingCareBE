package com.bookingcare.expertise.mapper;

import com.bookingcare.expertise.dto.*;
import com.bookingcare.expertise.entity.*;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class ExpertiseMapper {

    public DoctorsResponseDTO toDoctorsResponseDTO(Doctors doctors) {
        if (doctors == null) {
            return null;
        }

        return new DoctorsResponseDTO(
                doctors.getId() != null ? doctors.getId().toString() : null,
                doctors.getUserId(),
                doctors.getDoctorDetailInfor(),
                doctors.getShortDoctorInfor(),
                doctors.getSlug(),
                doctors.isDeleted()
        );
    }

    public Doctors toDoctorsEntity(DoctorsRequestDTO request) {
        if (request == null) {
            return null;
        }

        Doctors doctors = new Doctors();
        doctors.setUserId(request.userId());
        doctors.setDoctorDetailInfor(request.doctorDetailsInfor());
        doctors.setShortDoctorInfor(request.shortDoctorInfor());
        doctors.setSlug(generateSlug(request.shortDoctorInfor(), request.userId()));
        return doctors;
    }

    public void updateDoctorsFromRequest(DoctorsRequestDTO request, Doctors doctors) {
        if (request == null || doctors == null) {
            return;
        }

        doctors.setUserId(request.userId());
        doctors.setDoctorDetailInfor(request.doctorDetailsInfor());
        doctors.setShortDoctorInfor(request.shortDoctorInfor());
        doctors.setSlug(generateSlug(request.shortDoctorInfor(), doctors.getUserId()));
    }

    public SpecialtiesResponseDTO toSpecialtiesResponseDTO(Specialties specialties) {
        if (specialties == null) {
            return null;
        }

        return new SpecialtiesResponseDTO(
                specialties.getId() != null ? specialties.getId().toString() : null,
                specialties.getName(),
                specialties.getCode(),
                specialties.getSlug(),
                specialties.getSpecialtyDetailInfor(),
                specialties.getImage(),
                specialties.isDeleted(),
                specialties.getCreatedAt(),
                specialties.getUpdatedAt()
        );
    }

    public Specialties toSpecialtiesEntity(SpecialtiesRequestDTO request) {
        if (request == null) {
            return null;
        }

        Specialties specialties = new Specialties();
        specialties.setName(request.name());
        specialties.setCode(request.code());
        specialties.setSpecialtyDetailInfor(request.specialtyDetailInfor());
        specialties.setImage(request.image());
        specialties.setSlug(generateSlug(request.name(), request.code()));
        return specialties;
    }

    public void updateSpecialtiesFromRequest(SpecialtiesRequestDTO request, Specialties specialties) {
        if (request == null || specialties == null) {
            return;
        }

        specialties.setName(request.name());
        specialties.setCode(request.code());
        specialties.setSpecialtyDetailInfor(request.specialtyDetailInfor());
        specialties.setImage(request.image());
        specialties.setSlug(generateSlug(request.name(), specialties.getCode()));
    }

    public CredentialTypeResponseDTO toCredentialTypeResponseDTO(CredentialType credentialType) {
        if (credentialType == null) {
            return null;
        }

        return new CredentialTypeResponseDTO(
                credentialType.getId() != null ? credentialType.getId().toString() : null,
                credentialType.getCode(),
                credentialType.getName(),
                credentialType.getDescription(),
                credentialType.is_deleted(),
                credentialType.getCreatedAt(),
                credentialType.getUpdatedAt()
        );
    }

    public CredentialType toCredentialTypeEntity(CredentialTypeRequestDTO request) {
        if (request == null) {
            return null;
        }

        CredentialType credentialType = new CredentialType();
        credentialType.setCode(request.code());
        credentialType.setName(request.name());
        credentialType.setDescription(request.description());
        return credentialType;
    }

    public void updateCredentialTypeFromRequest(CredentialTypeRequestDTO request, CredentialType credentialType) {
        if (request == null || credentialType == null) {
            return;
        }

        credentialType.setCode(request.code());
        credentialType.setName(request.name());
        credentialType.setDescription(request.description());
    }

    public DoctorCredentialResponseDTO toDoctorCredentialResponseDTO(DoctorCredential credential) {
        if (credential == null) {
            return null;
        }

        return new DoctorCredentialResponseDTO(
                credential.getId() != null ? credential.getId().toString() : null,
                credential.getDoctor() != null && credential.getDoctor().getId() != null
                        ? credential.getDoctor().getId().toString()
                        : null,
                credential.getCredentialType() != null && credential.getCredentialType().getId() != null
                        ? credential.getCredentialType().getId().toString()
                        : null,
                credential.getLicenseNumber(),
                credential.getIssuer(),
                credential.getCountryCode(),
                credential.getRegion(),
                credential.getIssueDate(),
                credential.getExpiryDate(),
                credential.getStatus(),
                credential.getNote(),
                credential.isDeleted(),
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }

    public DoctorCredential toDoctorCredentialEntity(DoctorCredentialRequestDTO request) {
        if (request == null) {
            return null;
        }

        DoctorCredential credential = new DoctorCredential();
        credential.setDoctor(referenceDoctor(request.doctorId()));
        credential.setCredentialType(referenceCredentialType(request.credentialTypeId()));
        credential.setLicenseNumber(request.licenseNumber());
        credential.setIssuer(request.issuer());
        credential.setCountryCode(request.countryCode());
        credential.setRegion(request.region());
        credential.setIssueDate(request.issueDate());
        credential.setExpiryDate(request.expiryDate());
        credential.setStatus(request.status() != null ? request.status() : CredentialStatus.PENDING);
        credential.setNote(request.note());
        return credential;
    }

    public void updateDoctorCredentialFromRequest(DoctorCredentialRequestDTO request, DoctorCredential credential) {
        if (request == null || credential == null) {
            return;
        }

        Doctors doctorRef = referenceDoctor(request.doctorId());
        if (doctorRef != null) {
            credential.setDoctor(doctorRef);
        }

        CredentialType credentialTypeRef = referenceCredentialType(request.credentialTypeId());
        if (credentialTypeRef != null) {
            credential.setCredentialType(credentialTypeRef);
        }

        credential.setLicenseNumber(request.licenseNumber());
        credential.setIssuer(request.issuer());
        credential.setCountryCode(request.countryCode());
        credential.setRegion(request.region());
        credential.setIssueDate(request.issueDate());
        credential.setExpiryDate(request.expiryDate());
        if (request.status() != null) {
            credential.setStatus(request.status());
        }
        credential.setNote(request.note());
    }

    public DoctorCredentialFileResponseDTO toDoctorCredentialFileResponseDTO(DoctorCredentialFile file) {
        if (file == null) {
            return null;
        }

        return new DoctorCredentialFileResponseDTO(
                file.getId() != null ? file.getId().toString() : null,
                file.getDoctorCredential() != null && file.getDoctorCredential().getId() != null
                        ? file.getDoctorCredential().getId().toString()
                        : null,
                file.getFileName(),
                file.getContentType(),
                file.getFileUrl(),
                file.getUploadedAt()
        );
    }

    public DoctorCredentialFile toDoctorCredentialFileEntity(DoctorCredentialFileRequestDTO request) {
        if (request == null) {
            return null;
        }

        DoctorCredentialFile file = new DoctorCredentialFile();
        file.setDoctorCredential(referenceDoctorCredential(request.doctorCredentialId()));
        file.setFileName(request.fileName());
        file.setContentType(request.contentType());
        file.setFileUrl(request.fileUrl());
        return file;
    }

    public void updateDoctorCredentialFileFromRequest(DoctorCredentialFileRequestDTO request, DoctorCredentialFile file) {
        if (request == null || file == null) {
            return;
        }

        DoctorCredential credentialRef = referenceDoctorCredential(request.doctorCredentialId());
        if (credentialRef != null) {
            file.setDoctorCredential(credentialRef);
        }
        file.setFileName(request.fileName());
        file.setContentType(request.contentType());
        file.setFileUrl(request.fileUrl());
    }

    public DoctorCredentialVerificationResponseDTO toDoctorCredentialVerificationResponseDTO(DoctorCredentialVerification verification) {
        if (verification == null) {
            return null;
        }

        return new DoctorCredentialVerificationResponseDTO(
                verification.getId() != null ? verification.getId().toString() : null,
                verification.getDoctorCredential() != null && verification.getDoctorCredential().getId() != null
                        ? verification.getDoctorCredential().getId().toString()
                        : null,
                verification.getAction(),
                verification.getActorId(),
                verification.getComment(),
                verification.getCreatedAt()
        );
    }

    public DoctorCredentialVerification toDoctorCredentialVerificationEntity(DoctorCredentialVerificationRequestDTO request) {
        if (request == null) {
            return null;
        }

        DoctorCredentialVerification verification = new DoctorCredentialVerification();
        verification.setDoctorCredential(referenceDoctorCredential(request.doctorCredentialId()));
        verification.setAction(request.action());
        verification.setActorId(request.actorId());
        verification.setComment(request.comment());
        return verification;
    }

    private String generateSlug(String primarySource, String fallback) {
        String base = (primarySource != null && !primarySource.isBlank())
                ? primarySource
                : fallback;
        if (base == null || base.isBlank()) {
            return null;
        }

        String slug = base.trim().toLowerCase(Locale.ROOT);
        slug = slug.replaceAll("[^\\p{Alnum}]+", "-");
        slug = slug.replaceAll("(^-+|-+$)", "");
        return slug.isBlank() ? null : slug;
    }

    private Doctors referenceDoctor(String doctorId) {
        UUID id = stringToUUID(doctorId);
        if (id == null) {
            return null;
        }

        Doctors doctors = new Doctors();
        doctors.setId(id);
        return doctors;
    }

    private CredentialType referenceCredentialType(String credentialTypeId) {
        UUID id = stringToUUID(credentialTypeId);
        if (id == null) {
            return null;
        }

        CredentialType credentialType = new CredentialType();
        credentialType.setId(id);
        return credentialType;
    }

    private DoctorCredential referenceDoctorCredential(String doctorCredentialId) {
        UUID id = stringToUUID(doctorCredentialId);
        if (id == null) {
            return null;
        }

        DoctorCredential credential = new DoctorCredential();
        credential.setId(id);
        return credential;
    }

    private UUID stringToUUID(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    public DoctorCredentialWithFilesResponseDTO toDoctorCredentialWithFilesResponseDTO(DoctorCredential credential) {
        List<DoctorCredentialFileResponseDTO> files = credential.getFiles() == null
                ? List.of()
                : credential.getFiles().stream()
                        .map(this::toDoctorCredentialFileResponseDTO)
                        .toList();
        return new DoctorCredentialWithFilesResponseDTO(
                toDoctorCredentialResponseDTO(credential),
                files
        );
    }

    public DoctorCredentialFullResponseDTO toDoctorCredentialFullResponseDTO(DoctorCredential credential) {
        return new DoctorCredentialFullResponseDTO(
                toDoctorCredentialResponseDTO(credential),
                credential.getCredentialType() != null ? toCredentialTypeResponseDTO(credential.getCredentialType()) : null,
                credential.getFiles() == null ? List.of() :
                        credential.getFiles().stream()
                                .map(this::toDoctorCredentialFileResponseDTO)
                                .toList(),
                credential.getVerifications() == null ? List.of() :
                        credential.getVerifications().stream()
                                .map(this::toDoctorCredentialVerificationResponseDTO)
                                .toList()
        );
    }



}
