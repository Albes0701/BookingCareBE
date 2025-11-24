package com.bookingcare.clinic.repository;

import com.bookingcare.clinic.entity.ClinicAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicAccountRepository extends JpaRepository<ClinicAccount, String> {
    
    /**
     * Find clinic account mapping by accountId
     * @param accountId ID from Account/Identity Service
     * @return ClinicAccount if exists
     */
    Optional<ClinicAccount> findByAccountIdAndIsDeletedFalse(String accountId);
    
    /**
     * Find clinic account mapping by clinicId and accountId
     * @param clinicId clinic ID
     * @param accountId account ID
     * @return ClinicAccount if exists
     */
    Optional<ClinicAccount> findByClinicIdAndAccountIdAndIsDeletedFalse(String clinicId, String accountId);
}
