package com.bookingcare.expertise.service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookingcare.expertise.dto.CredentialTypeResponseDTO;
import com.bookingcare.expertise.dto.DoctorCredentialFileRequestDTO;
import com.bookingcare.expertise.dto.DoctorCredentialFullResponseDTO;
import com.bookingcare.expertise.dto.DoctorCredentialRequestDTO;
import com.bookingcare.expertise.dto.DoctorCredentialWithFilesResponseDTO;
import com.bookingcare.expertise.dto.DoctorProfileResponseDTO;
import com.bookingcare.expertise.dto.DoctorsRequestDTO;
import com.bookingcare.expertise.dto.DoctorsResponseDTO;
import com.bookingcare.expertise.dto.SpecialtiesResponseDTO;
import com.bookingcare.expertise.entity.CredentialStatus;
import com.bookingcare.expertise.entity.CredentialType;
import com.bookingcare.expertise.entity.DoctorCredential;
import com.bookingcare.expertise.entity.DoctorCredentialFile;
import com.bookingcare.expertise.entity.DoctorCredentialVerification;
import com.bookingcare.expertise.entity.Doctors;
import com.bookingcare.expertise.entity.Doctors_Specialties;
import com.bookingcare.expertise.entity.Specialties;
import com.bookingcare.expertise.entity.SpecialtyStatus;
import com.bookingcare.expertise.entity.VerificationAction;
import com.bookingcare.expertise.exception.ApiException;
import com.bookingcare.expertise.exception.ErrorCode;
import com.bookingcare.expertise.mapper.ExpertiseMapper;
import com.bookingcare.expertise.repository.CredentialTypeRepo;
import com.bookingcare.expertise.repository.DoctorCredentialFileRepo;
import com.bookingcare.expertise.repository.DoctorCredentialRepo;
import com.bookingcare.expertise.repository.DoctorCredentialVerificationRepo;
import com.bookingcare.expertise.repository.Doctor_Specialty_Repo;
import com.bookingcare.expertise.repository.DoctorsRepo;
import com.bookingcare.expertise.repository.SpecialtyRepo;
import org.springframework.util.StringUtils;


@Service
public class ExpertiseService {

    private final DoctorsRepo doctorsRepo;
    private final SpecialtyRepo specialtiesRepo;
    private final DoctorCredentialRepo doctorCredentialRepo;
    private final CredentialTypeRepo credentialTypeRepo;
    private final DoctorCredentialFileRepo doctorCredentialFileRepo;
    private final Doctor_Specialty_Repo doctorsSpecialtiesRepo;
    private final DoctorCredentialVerificationRepo doctorCredentialVerificationRepo;
    

    private final ExpertiseMapper expertiseMapper;

    public ExpertiseService(
                            DoctorsRepo doctorsRepo, SpecialtyRepo specialtiesRepo, 
                            ExpertiseMapper expertiseMapper, DoctorCredentialRepo doctorCredentialRepo, 
                            CredentialTypeRepo credentialTypeRepo,
                            DoctorCredentialFileRepo doctorCredentialFileRepo, 
                            Doctor_Specialty_Repo doctorsSpecialtiesRepo, 
                            DoctorCredentialVerificationRepo doctorCredentialVerificationRepo
                     ) {
        this.doctorsRepo = doctorsRepo;
        this.specialtiesRepo = specialtiesRepo;
        this.doctorCredentialRepo = doctorCredentialRepo;
        this.credentialTypeRepo = credentialTypeRepo;
        this.doctorCredentialFileRepo = doctorCredentialFileRepo;
        this.doctorsSpecialtiesRepo = doctorsSpecialtiesRepo;
        this.doctorCredentialVerificationRepo = doctorCredentialVerificationRepo;
        this.expertiseMapper = expertiseMapper;
    }

    // ========== PUBLIC ========== //

    // Doctors

    private Doctors resolveDoctor(String idOrSlug) {
        return tryParseUuid(idOrSlug)
                .flatMap(doctorsRepo::findById)
                .or(() -> doctorsRepo.findBySlug(idOrSlug))
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));
    }

    private Optional<UUID> tryParseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public List<DoctorsResponseDTO> getAllDoctors() {
        return doctorsRepo.findAll()
                .stream()
                .map(expertiseMapper::toDoctorsResponseDTO)
                .toList();
    }

    public DoctorsResponseDTO getDoctorByIdOrSlug(String idOrSlug) {
        var doctor = resolveDoctor(idOrSlug);
        return expertiseMapper.toDoctorsResponseDTO(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorCredentialWithFilesResponseDTO> getApprovedCredentials(String idOrSlug) {
        Doctors doctor = resolveDoctor(idOrSlug);
        LocalDate today = LocalDate.now();
        return doctorCredentialRepo.findAllByDoctorIdAndStatus(doctor.getId(), CredentialStatus.APPROVED)
                .stream()
                .filter(credential -> credential.getExpiryDate() == null || !credential.getExpiryDate().isBefore(today))
                .map(expertiseMapper::toDoctorCredentialWithFilesResponseDTO)
                .toList();
    }

    // Specialties
    public List<SpecialtiesResponseDTO> getAllSpecialties() {
        return specialtiesRepo.findAll()
                .stream()
                .map(expertiseMapper::toSpecialtiesResponseDTO)
                .toList();
    }

    public SpecialtiesResponseDTO getSpecialtyBySlugOrCode(String slugOrCode) {
        Specialties specialty = specialtiesRepo.findByCodeIgnoreCase(slugOrCode)
                .or(() -> specialtiesRepo.findBySlug(slugOrCode))
                .filter(s -> s.getStatus() == SpecialtyStatus.APPROVED)
                .orElseThrow(() -> new ApiException(ErrorCode.SPECIALTY_NOT_FOUND));
            return expertiseMapper.toSpecialtiesResponseDTO(specialty);
    }



    // ========== DOCTORS ========== //

    // Lấy thông tin bác sĩ theo userId (tài khoản)
    public DoctorsResponseDTO getDoctorByUserId(String userId) {
        Doctors doctor = doctorsRepo.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));
        return expertiseMapper.toDoctorsResponseDTO(doctor);
    }

    // Hồ sơ bác sĩ (profile)

    @Transactional(readOnly = true)
    public DoctorProfileResponseDTO getDoctorProfileByUserId(String userId) {
        Doctors doctor = doctorsRepo.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        List<DoctorCredentialFullResponseDTO> credentials = doctorCredentialRepo
                .findAllByDoctorId(doctor.getId())
                .stream()
                .map(expertiseMapper::toDoctorCredentialFullResponseDTO)
                .toList();

        return new DoctorProfileResponseDTO(
                expertiseMapper.toDoctorsResponseDTO(doctor),
                credentials
        );
    }

    @Transactional(readOnly = true)
    public List<CredentialTypeResponseDTO> getAllCredentialTypes() {
        return credentialTypeRepo.findAll()
                .stream()
                .map(expertiseMapper::toCredentialTypeResponseDTO)
                .toList();
    }

    // CẬP NHẬT - TẠO MỚI BÁC SĨ

    private String generateSlug(String fullName, String fallbackKey) {
        String base = StringUtils.hasText(fullName)
                ? fullName
                : (StringUtils.hasText(fallbackKey) ? fallbackKey : UUID.randomUUID().toString());

        // Bỏ dấu, đưa về lower-case, thay chuỗi không phải a-z0-9 thành dấu '-'
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (normalized.isBlank()) {
            normalized = UUID.randomUUID().toString();
        }

        // Đảm bảo slug không trùng (thêm hậu tố -1, -2,... nếu cần)
        String candidate = normalized;
        int counter = 1;
        while (doctorsRepo.findBySlug(candidate).isPresent()) {
            candidate = normalized + "-" + counter++;
        }
        return candidate;
    }


    // Thêm mới bác sĩ
    @Transactional
    public DoctorsResponseDTO createDoctor(DoctorsRequestDTO request) {
        if (doctorsRepo.findByUserId(request.userId()).isPresent()) {
            throw new ApiException(ErrorCode.DOCTOR_ALREADY_EXISTS);
        }

        Specialties specialty = specialtiesRepo.findById(UUID.fromString(request.specialtyId()))
                .orElseThrow(() -> new ApiException(ErrorCode.SPECIALTY_NOT_FOUND));

        Doctors doctor = new Doctors();
        doctor.setUserId(request.userId());
        doctor.setShortDoctorInfor(request.shortDoctorInfor());
        doctor.setDoctorDetailInfor(request.doctorDetailsInfor());
        doctor.setSlug(generateSlug(request.fullName(), request.userId()));

        Doctors saved = doctorsRepo.save(doctor);

        Doctors_Specialties doctorSpecialty = new Doctors_Specialties();
        doctorSpecialty.setDoctors(doctor);
        doctorSpecialty.setSpecialties(specialty);

        doctorsSpecialtiesRepo.save(doctorSpecialty);
        return expertiseMapper.toDoctorsResponseDTO(saved);
    }

    // Cập nhật bác sĩ
    @Transactional
    public DoctorsResponseDTO updateDoctor(String id, DoctorsRequestDTO request) {
        UUID doctorId = UUID.fromString(id);
        Doctors existingDoctor = doctorsRepo.findByIdAndDeletedFalse(doctorId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        existingDoctor.setShortDoctorInfor(request.shortDoctorInfor());
        existingDoctor.setDoctorDetailInfor(request.doctorDetailsInfor());
        existingDoctor.setSlug(generateSlug(request.fullName(), request.userId()));

        Doctors savedDoctor = doctorsRepo.save(existingDoctor);

        UUID specialtyId = UUID.fromString(request.specialtyId());
        Specialties specialty = specialtiesRepo.findById(specialtyId)
                .orElseThrow(() -> new ApiException(ErrorCode.SPECIALTY_NOT_FOUND));

        doctorsSpecialtiesRepo.findByDoctorsIdAndSpecialtiesId(savedDoctor.getId(), specialty.getId())
                .orElseGet(() -> doctorsSpecialtiesRepo.save(
                        Doctors_Specialties.builder()
                                .doctors(savedDoctor)
                                .specialties(specialty)
                                .build()
                ));

        return expertiseMapper.toDoctorsResponseDTO(savedDoctor);
    }

    // Xóa bác sĩ
    @Transactional
    public void deleteDoctor(String id) {
        Doctors existingDoctor = doctorsRepo.findByIdAndDeletedFalse(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        existingDoctor.setDeleted(true);
        doctorsRepo.save(existingDoctor);
    }

    // Lấy thông tin bác sĩ theo userId
    public DoctorsResponseDTO getDoctorByUserIdSimple(String userId) {
        Doctors doctor = doctorsRepo.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));
        return expertiseMapper.toDoctorsResponseDTO(doctor);
    }

    // Lấy thông tin bác sĩ theo code hoặc slug chuyên khoa
   @Transactional(readOnly = true)
    public List<DoctorsResponseDTO> getDoctorsBySpecialtyId(String codeOrSlug) {
        Specialties specialty = specialtiesRepo.findByCodeIgnoreCase(codeOrSlug)
                .or(() -> specialtiesRepo.findBySlug(codeOrSlug))
                .filter(s -> !s.isDeleted() && s.getStatus() == SpecialtyStatus.APPROVED)
                .orElseThrow(() -> new ApiException(ErrorCode.SPECIALTY_NOT_FOUND));

        return doctorsSpecialtiesRepo.findAllBySpecialties(specialty).stream()
                .map(Doctors_Specialties::getDoctors)
                .filter(Objects::nonNull)
                .filter(doctor -> !doctor.isDeleted())   // optional guard, since Doctors has SQLRestriction
                .map(expertiseMapper::toDoctorsResponseDTO)
                .toList();
    }


    // CẬP NHẬT - THÊM MỚI CHỨNG CHỈ

    // Lấy danh sách chứng chỉ của bác sĩ
    @Transactional(readOnly = true)
    public List<DoctorCredentialWithFilesResponseDTO> getDoctorCredentials(String doctorId) {
        Doctors doctor = doctorsRepo.findByIdAndDeletedFalse(UUID.fromString(doctorId))
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        return doctorCredentialRepo.findAllByDoctorId(doctor.getId()).stream()
                .map(expertiseMapper::toDoctorCredentialWithFilesResponseDTO)
                .toList();
    }

    // Thêm chứng chỉ cho bác sĩ
    @Transactional
    public DoctorCredentialWithFilesResponseDTO createDoctorCredential(String doctorId, DoctorCredentialRequestDTO request) {
        Doctors doctor = doctorsRepo.findByIdAndDeletedFalse(UUID.fromString(doctorId))
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        CredentialType credentialType = credentialTypeRepo.findById(UUID.fromString(request.credentialTypeId()))
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_TYPE_NOT_FOUND));
        
        DoctorCredential newCredential = new DoctorCredential();
        newCredential.setDoctor(doctor);
        newCredential.setCredentialType(credentialType);
        newCredential.setLicenseNumber(request.licenseNumber());
        newCredential.setIssuer(request.issuer());
        newCredential.setCountryCode(request.countryCode());
        newCredential.setRegion(request.region());
        newCredential.setIssueDate(request.issueDate());
        newCredential.setExpiryDate(request.expiryDate());
        newCredential.setStatus(CredentialStatus.DRAFT); // Mặc định là DRAFT khi tạo mới
        newCredential.setNote(request.note());

        DoctorCredential savedCredential = doctorCredentialRepo.save(newCredential);

        return expertiseMapper.toDoctorCredentialWithFilesResponseDTO(savedCredential);
    
    }

    // Thêm file cho chứng chỉ bác sĩ
    @Transactional
    public DoctorCredentialWithFilesResponseDTO addFileToDoctorCredential(String credentialId, DoctorCredentialFileRequestDTO fileRequest) {

        DoctorCredential existingCredential = doctorCredentialRepo.findByIdAndDeletedFalse(UUID.fromString(credentialId))
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_NOT_FOUND));

        DoctorCredentialFile credentialFile = new DoctorCredentialFile();
        credentialFile.setFileUrl(fileRequest.fileUrl());
        credentialFile.setContentType(fileRequest.contentType());
        credentialFile.setFileName(fileRequest.fileName());
        credentialFile.setDoctorCredential(existingCredential);
        doctorCredentialFileRepo.save(credentialFile);

        return expertiseMapper.toDoctorCredentialWithFilesResponseDTO(existingCredential);
    }

    // Cập nhật thông tin chứng chỉ của bác sĩ
    @Transactional
    public DoctorCredentialWithFilesResponseDTO updateDoctorCredential(String doctorId, String credentialId, DoctorCredentialRequestDTO request) {
        Doctors doctor = doctorsRepo.findByIdAndDeletedFalse(UUID.fromString(doctorId))
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        DoctorCredential existingCredential = doctorCredentialRepo.findByIdAndDoctorIdAndDeletedFalse(UUID.fromString(credentialId), doctor.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_NOT_FOUND));

        // Cập nhật thông tin chứng chỉ
        existingCredential.setLicenseNumber(request.licenseNumber());
        existingCredential.setIssuer(request.issuer());
        existingCredential.setCountryCode(request.countryCode());
        existingCredential.setRegion(request.region());
        existingCredential.setIssueDate(request.issueDate());
        existingCredential.setExpiryDate(request.expiryDate());
        existingCredential.setNote(request.note());

        DoctorCredential updatedCredential = doctorCredentialRepo.save(existingCredential);

        return expertiseMapper.toDoctorCredentialWithFilesResponseDTO(updatedCredential);
    }

    // Xóa chứng chỉ của bác sĩ
    @Transactional
    public void deleteDoctorCredential(String doctorId, String credentialId) {
        Doctors doctor = doctorsRepo.findByIdAndDeletedFalse(UUID.fromString(doctorId))
                .orElseThrow(() -> new ApiException(ErrorCode.DOCTOR_NOT_FOUND));

        DoctorCredential existingCredential = doctorCredentialRepo.findByIdAndDoctorIdAndDeletedFalse(UUID.fromString(credentialId), doctor.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_NOT_FOUND));
        existingCredential.setDeleted(true);
        doctorCredentialRepo.save(existingCredential);    

        // Xóa file chứng chỉ nếu có
        doctorCredentialFileRepo.findByDoctorCredential(existingCredential)
        .ifPresent(doctorCredentialFileRepo::delete);
        
    }

    // Xóa file minh chứng chứng chỉ của bác sĩ
    @Transactional
    public void deleteFileFromDoctorCredential(String fileId) {
        doctorCredentialFileRepo.findById(UUID.fromString(fileId))
        .ifPresent(doctorCredentialFileRepo::delete);
    }

    // Nộp chứng chỉ để duyệt
    @Transactional
    public DoctorCredentialWithFilesResponseDTO submitDoctorCredential(String credentialId) {
        DoctorCredential credential = doctorCredentialRepo
                .findByIdAndDeletedFalse(UUID.fromString(credentialId))
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_NOT_FOUND));

        if (credential.getStatus() != CredentialStatus.DRAFT) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Credential is not in draft state");
        }

        credential.setStatus(CredentialStatus.PENDING);
        DoctorCredential saved = doctorCredentialRepo.save(credential);
        return expertiseMapper.toDoctorCredentialWithFilesResponseDTO(saved);
    }

    // ========== ADMIN ========== //
    private void logVerification(DoctorCredential credential,
                             VerificationAction action,
                             UUID actorId,
                             String comment) {
        DoctorCredentialVerification verification = DoctorCredentialVerification.builder()
                .doctorCredential(credential)
                .action(action)
                .actorId(actorId)
                .comment(comment)
                .build();
        doctorCredentialVerificationRepo.save(verification);
    }

    // Approve credential
    @Transactional
    public DoctorCredentialWithFilesResponseDTO approveDoctorCredential(String credentialId,
                                                                        UUID actorId,
                                                                        String comment) {
        DoctorCredential credential = doctorCredentialRepo
                .findById(UUID.fromString(credentialId))
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_NOT_FOUND));

        if (credential.getStatus() != CredentialStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Only credentials in PENDING state can be approved");
        }

        credential.setStatus(CredentialStatus.APPROVED);
        DoctorCredential saved = doctorCredentialRepo.save(credential);

        logVerification(saved, VerificationAction.APPROVE, actorId, comment);

        return expertiseMapper.toDoctorCredentialWithFilesResponseDTO(saved);
    }

    // Reject a pending credential
    @Transactional
    public DoctorCredentialWithFilesResponseDTO rejectDoctorCredential(String credentialId,
                                                                    UUID actorId,
                                                                    String reason) {
        DoctorCredential credential = doctorCredentialRepo
                .findById(UUID.fromString(credentialId))
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENTIAL_NOT_FOUND));

        if (credential.getStatus() != CredentialStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Only credentials in PENDING state can be rejected");
        }
        if (!StringUtils.hasText(reason)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Rejection reason must be provided");
        }

        credential.setStatus(CredentialStatus.REJECTED);
        DoctorCredential saved = doctorCredentialRepo.save(credential);

        logVerification(saved, VerificationAction.REJECT, actorId, reason);

        return expertiseMapper.toDoctorCredentialWithFilesResponseDTO(saved);
    }

    // Lấy danh sách chứng chỉ đang chờ duyệt với bộ lọc
    @Transactional(readOnly = true)
    public List<DoctorCredentialWithFilesResponseDTO> getPendingCredentials(String credentialTypeCode,
                                                                            String doctorId,
                                                                            LocalDate expiryDateBefore) {
        List<DoctorCredential> pendingCredentials = doctorCredentialRepo.findAllByStatus(CredentialStatus.PENDING);

        return pendingCredentials.stream()
                .filter(credential -> {
                    if (StringUtils.hasText(credentialTypeCode)) {
                        if (credential.getCredentialType() == null
                                || !credentialTypeCode.equalsIgnoreCase(credential.getCredentialType().getCode())) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(credential -> {
                    if (StringUtils.hasText(doctorId)) {
                        UUID doctorUuid;
                        try {
                            doctorUuid = UUID.fromString(doctorId);
                        } catch (IllegalArgumentException ex) {
                            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid doctorId value");
                        }
                        return credential.getDoctor() != null
                                && Objects.equals(credential.getDoctor().getId(), doctorUuid);
                    }
                    return true;
                })
                .filter(credential -> {
                    if (expiryDateBefore != null) {
                        return credential.getExpiryDate() != null
                                && credential.getExpiryDate().isBefore(expiryDateBefore);
                    }
                    return true;
                })
                .map(expertiseMapper::toDoctorCredentialWithFilesResponseDTO)
                .toList();
    }




}
