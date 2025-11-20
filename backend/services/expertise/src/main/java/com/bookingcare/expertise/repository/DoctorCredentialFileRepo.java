package com.bookingcare.expertise.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.expertise.entity.DoctorCredential;
import com.bookingcare.expertise.entity.DoctorCredentialFile;

public interface DoctorCredentialFileRepo extends JpaRepository<DoctorCredentialFile, UUID> {

    Optional<DoctorCredentialFile> findByDoctorCredential(DoctorCredential doctorCredential);
}
