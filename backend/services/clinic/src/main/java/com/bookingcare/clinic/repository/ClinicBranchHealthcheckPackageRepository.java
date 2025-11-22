package com.bookingcare.clinic.repository;

import com.bookingcare.clinic.dto.ClinicPackageResponse;
import com.bookingcare.clinic.entity.ClinicBranchHealthcheckPackage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicBranchHealthcheckPackageRepository extends JpaRepository<ClinicBranchHealthcheckPackage, String> {

    List<ClinicPackageResponse> findPackageIdByClinicBranchId(String clinicBranchId);

}
