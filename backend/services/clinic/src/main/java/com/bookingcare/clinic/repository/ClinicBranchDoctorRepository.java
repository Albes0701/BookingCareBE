package com.bookingcare.clinic.repository;

import com.bookingcare.clinic.entity.ClinicBranchDoctor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicBranchDoctorRepository extends JpaRepository<ClinicBranchDoctor, String> {

    List<ClinicBranchDoctor> findByClinicBranch_Id(String clinicBranchId);

    List<ClinicBranchDoctor> findByClinicBranch_IdAndIsDeleted(String clinicBranchId, boolean isDeleted);

    Optional<ClinicBranchDoctor> findByClinicBranch_IdAndDoctorId(String clinicBranchId, String doctorId);

    Optional<ClinicBranchDoctor> findByIdAndClinicBranch_Id(String id, String clinicBranchId);
}
