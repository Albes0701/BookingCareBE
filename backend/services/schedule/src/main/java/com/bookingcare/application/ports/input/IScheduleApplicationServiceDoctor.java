package com.bookingcare.application.ports.input;

import java.util.List;

import com.bookingcare.application.dto.QueryPackageScheduleResponse;

public interface IScheduleApplicationServiceDoctor {

    // Register schedules for doctor
    boolean registerDoctorSchedules(String doctorId, List<String> scheduleIds);


    // Get all package schedules by doctorId
    List<QueryPackageScheduleResponse> getAllSchedulesByDoctorId(String doctorId);
    

    // Delete doctor package schedule by scheduleId ownered by doctor
    boolean deleteSchedulePackageDoctorById(String doctorId, String packageScheduleId);
 



}
