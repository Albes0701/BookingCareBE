package com.bookingcare.application.ports.input;

import java.util.List;

import com.bookingcare.application.dto.PackageScheduleRequest;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;

public interface IScheduleApplicationServiceClinicAdmin {


    // Get all schedules by clinic branch
    List<QueryPackageScheduleResponse> getAllSchedulesByClinicBranchId(String clinicBranchId);

    // Create new package schedule
    QueryPackageScheduleResponse createPackageSchedule(PackageScheduleRequest request);


    // Update existing package schedule
    QueryPackageScheduleResponse updatePackageSchedule(String packageScheduleId, PackageScheduleRequest request);
    
    // Delete package schedule by id
    boolean deletePackageSchedule(String packageScheduleId);

    // Register doctors to package schedule
    boolean registerDoctorsToPackageSchedule(String packageScheduleId, List<String> doctorIds);


}
