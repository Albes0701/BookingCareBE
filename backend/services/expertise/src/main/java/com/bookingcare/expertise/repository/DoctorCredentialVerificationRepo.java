package com.bookingcare.expertise.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.expertise.entity.DoctorCredentialVerification;

public interface DoctorCredentialVerificationRepo extends JpaRepository<DoctorCredentialVerification, UUID> {
    
}
