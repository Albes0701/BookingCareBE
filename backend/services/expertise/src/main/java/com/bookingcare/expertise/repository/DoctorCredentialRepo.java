package com.bookingcare.expertise.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.expertise.entity.CredentialStatus;
import com.bookingcare.expertise.entity.DoctorCredential;

public interface DoctorCredentialRepo extends JpaRepository<DoctorCredential, UUID> {

    List<DoctorCredential> findAllByDoctorIdAndStatus(UUID doctorId, CredentialStatus status);
    Optional<DoctorCredential> findByDoctorIdAndCredentialTypeIdAndStatus(UUID doctorId, UUID typeId, CredentialStatus status);
    Optional<DoctorCredential> findByIdAndStatus(UUID id, CredentialStatus status);
    Optional<DoctorCredential> findByDoctorIdAndLicenseNumber(UUID doctorId, String licenseNumber);
    Optional<DoctorCredential> findByDoctorIdAndCredentialTypeId(UUID doctorId, UUID credentialTypeId);

    @EntityGraph(attributePaths = "credentialType")
    List<DoctorCredential> findAllByDoctorId(UUID doctorId);

    Optional<DoctorCredential> findByIdAndDeletedFalse(UUID id);

    @EntityGraph(attributePaths = {"credentialType", "doctor", "files"})
    List<DoctorCredential> findAllByStatus(CredentialStatus status);

    Optional<DoctorCredential> findByIdAndDoctorIdAndDeletedFalse(UUID id, UUID doctorId);
    
}
