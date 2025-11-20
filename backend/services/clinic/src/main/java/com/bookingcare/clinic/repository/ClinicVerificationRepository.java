package com.bookingcare.clinic.repository;

import com.bookingcare.clinic.entity.ClinicVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClinicVerificationRepository extends JpaRepository<ClinicVerification, UUID> {
}
