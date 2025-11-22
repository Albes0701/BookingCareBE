package com.bookingcare.container.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.ports.input.IScheduleApplicationServiceDoctor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/schedule/doctor")
@Slf4j
public class ScheduleControllerDoctor {
    private final IScheduleApplicationServiceDoctor _scheduleApplicationServiceDoctor;

    /**
     * Register multiple package schedules to a doctor
     * Assigns a doctor to multiple health check package schedules
     * 
     * @param doctorId The ID of the doctor
     * @param packageScheduleIds List of package schedule IDs to register
     * @return Success status of the registration
     */
    @PostMapping("/register/{doctorId}")
    public ApiResponse<String> registerDoctorSchedules(
            @PathVariable String doctorId,
            @RequestBody List<String> packageScheduleIds) {
        log.info("Registering doctor {} to {} schedules", doctorId, 
                packageScheduleIds != null ? packageScheduleIds.size() : 0);

        // Validate input
        if (doctorId == null || doctorId.isEmpty()) {
            log.warn("Invalid doctor ID provided");
            return new ApiResponse<>(400, "Invalid doctor ID", null);
        }

        if (packageScheduleIds == null || packageScheduleIds.isEmpty()) {
            log.warn("No package schedule IDs provided");
            return new ApiResponse<>(400, "Package schedule IDs list cannot be empty", null);
        }

        try {
            boolean isSuccess = _scheduleApplicationServiceDoctor.registerDoctorSchedules(doctorId, packageScheduleIds);

            if (!isSuccess) {
                log.error("Failed to register doctor {} to schedules", doctorId);
                return new ApiResponse<>(500, "Failed to register doctor schedules", null);
            }

            log.info("Successfully registered doctor {} to {} schedules", doctorId, packageScheduleIds.size());
            return new ApiResponse<>(200, "Doctor schedules registered successfully", null);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return new ApiResponse<>(400, "Validation error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error registering doctor schedules: {}", e.getMessage());
            return new ApiResponse<>(500, "Failed to register doctor schedules", null);
        }
    }

    /**
     * Get all package schedules assigned to a doctor
     * Retrieves all health check package slots where the doctor is assigned
     * 
     * @param doctorId The ID of the doctor
     * @return List of QueryPackageScheduleResponse containing schedule information
     */
    @GetMapping("/{doctorId}")
    public ApiResponse<List<QueryPackageScheduleResponse>> getAllSchedulesByDoctorId(
            @PathVariable String doctorId) {
        log.info("Fetching all schedules for doctor: {}", doctorId);

        // Validate input
        if (doctorId == null || doctorId.isEmpty()) {
            log.warn("Invalid doctor ID provided");
            return new ApiResponse<>(400, "Invalid doctor ID", null);
        }

        try {
            List<QueryPackageScheduleResponse> schedules = _scheduleApplicationServiceDoctor
                    .getAllSchedulesByDoctorId(doctorId);

            if (schedules == null || schedules.isEmpty()) {
                log.info("No schedules found for doctor: {}", doctorId);
                return new ApiResponse<>(200, "No schedules found", schedules);
            }

            log.info("Successfully fetched {} schedules for doctor: {}", schedules.size(), doctorId);
            return new ApiResponse<>(200, "Success", schedules);

        } catch (Exception e) {
            log.error("Error fetching schedules for doctor {}: {}", doctorId, e.getMessage());
            return new ApiResponse<>(500, "Failed to fetch doctor schedules", null);
        }
    }

    /**
     * Delete a package schedule assignment for a doctor
     * Removes a doctor from a specific health check package schedule
     * 
     * @param doctorId The ID of the doctor
     * @param packageScheduleId The ID of the package schedule to delete
     * @return Success status of the deletion
     */
    @DeleteMapping("/{doctorId}/{packageScheduleId}")
    public ApiResponse<String> deleteSchedulePackageDoctorById(
            @PathVariable String doctorId,
            @PathVariable String packageScheduleId) {
        log.info("Deleting schedule assignment for doctor {} and package schedule {}", 
                doctorId, packageScheduleId);

        // Validate input
        if (doctorId == null || doctorId.isEmpty()) {
            log.warn("Invalid doctor ID provided");
            return new ApiResponse<>(400, "Invalid doctor ID", null);
        }

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        try {
            boolean isSuccess = _scheduleApplicationServiceDoctor
                    .deleteSchedulePackageDoctorById(doctorId, packageScheduleId);

            if (!isSuccess) {
                log.warn("Assignment not found for doctor {} and schedule {}", doctorId, packageScheduleId);
                return new ApiResponse<>(404, "Assignment not found", null);
            }

            log.info("Successfully deleted assignment for doctor {} and schedule {}", doctorId, packageScheduleId);
            return new ApiResponse<>(200, "Doctor schedule assignment deleted successfully", null);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return new ApiResponse<>(400, "Validation error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error deleting assignment for doctor {} and schedule {}: {}", 
                    doctorId, packageScheduleId, e.getMessage());
            return new ApiResponse<>(500, "Failed to delete doctor schedule assignment", null);
        }
    }
}
