package com.bookingcare.clinic.repository;

import com.bookingcare.clinic.entity.ClinicBranch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicBranchRepository extends JpaRepository<ClinicBranch, String> {

    List<ClinicBranch> findByClinic_Id(String clinicId);

    Optional<ClinicBranch> findByIdAndClinic_Id(String branchId, String clinicId);

    List<ClinicBranch> findByClinic_IdAndIsDeletedFalse(String clinicId);

    Optional<ClinicBranch> findByIdAndClinic_IdAndIsDeletedFalse(String branchId, String clinicId);
}
