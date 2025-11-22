package com.bookingcare.container.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.PackageScheduleRequest;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.ports.input.IScheduleApplicationServiceClinicAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/schedule/admin")
@Slf4j
public class ScheduleControllerClinicAdmin {
    private final IScheduleApplicationServiceClinicAdmin _scheduleApplicationServiceClinicAdmin;

    /**
     * Get all package schedules for a clinic branch
     * Retrieves all health check package schedules associated with a specific clinic branch
     * 
     * @param clinicBranchId The ID of the clinic branch
     * @return List of QueryPackageScheduleResponse containing schedule information
     */
    @GetMapping("/clinic-branch/{clinicBranchId}")
    public ApiResponse<List<QueryPackageScheduleResponse>> getAllSchedulesByClinicBranchId(
            @PathVariable String clinicBranchId) {
        log.info("Fetching all schedules for clinic branch: {}", clinicBranchId);

        if (clinicBranchId == null || clinicBranchId.isEmpty()) {
            log.warn("Invalid clinic branch ID provided");
            return new ApiResponse<>(400, "Invalid clinic branch ID", null);
        }

        try {
            List<QueryPackageScheduleResponse> schedules = _scheduleApplicationServiceClinicAdmin
                    .getAllSchedulesByClinicBranchId(clinicBranchId);

            if (schedules == null || schedules.isEmpty()) {
                log.info("No schedules found for clinic branch: {}", clinicBranchId);
                return new ApiResponse<>(200, "No schedules found", schedules);
            }

            log.info("Successfully fetched {} schedules for clinic branch: {}", schedules.size(), clinicBranchId);
            return new ApiResponse<>(200, "Success", schedules);

        } catch (Exception e) {
            log.error("Error fetching schedules for clinic branch {}: {}", clinicBranchId, e.getMessage());
            return new ApiResponse<>(500, "Failed to fetch schedules", null);
        }
    }

    /**
     * Create a new package schedule
     * Creates a new health check package schedule with specified details
     * 
     * @param request PackageScheduleRequest containing schedule details
     * @return QueryPackageScheduleResponse with created schedule information
     */
    @PostMapping("/create")
    public ApiResponse<QueryPackageScheduleResponse> createPackageSchedule(
            @RequestBody PackageScheduleRequest request) {
        log.info("Creating new package schedule");

        if (request == null) {
            log.warn("Package schedule request is null");
            return new ApiResponse<>(400, "Package schedule request cannot be null", null);
        }

        try {
            QueryPackageScheduleResponse response = _scheduleApplicationServiceClinicAdmin
                    .createPackageSchedule(request);

            log.info("Successfully created package schedule");
            return new ApiResponse<>(201, "Package schedule created successfully", response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return new ApiResponse<>(400, "Validation error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error creating package schedule: {}", e.getMessage());
            return new ApiResponse<>(500, "Failed to create package schedule", null);
        }
    }

    /**
     * Update an existing package schedule
     * Modifies details of an existing health check package schedule
     * 
     * @param packageScheduleId The ID of the package schedule to update
     * @param request PackageScheduleRequest containing updated schedule details
     * @return QueryPackageScheduleResponse with updated schedule information
     */
    @PutMapping("/{packageScheduleId}")
    public ApiResponse<QueryPackageScheduleResponse> updatePackageSchedule(
            @PathVariable String packageScheduleId,
            @RequestBody PackageScheduleRequest request) {
        log.info("Updating package schedule: {}", packageScheduleId);

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        if (request == null) {
            log.warn("Package schedule request is null");
            return new ApiResponse<>(400, "Package schedule request cannot be null", null);
        }

        try {
            QueryPackageScheduleResponse response = _scheduleApplicationServiceClinicAdmin
                    .updatePackageSchedule(packageScheduleId, request);

            log.info("Successfully updated package schedule: {}", packageScheduleId);
            return new ApiResponse<>(200, "Package schedule updated successfully", response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return new ApiResponse<>(400, "Validation error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error updating package schedule {}: {}", packageScheduleId, e.getMessage());
            return new ApiResponse<>(500, "Failed to update package schedule", null);
        }
    }

    /**
     * Delete a package schedule
     * Removes/deletes a health check package schedule by ID
     * 
     * @param packageScheduleId The ID of the package schedule to delete
     * @return Success status of the deletion
     */
    @DeleteMapping("/{packageScheduleId}")
    public ApiResponse<String> deletePackageSchedule(@PathVariable String packageScheduleId) {
        log.info("Deleting package schedule: {}", packageScheduleId);

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        try {
            boolean isSuccess = _scheduleApplicationServiceClinicAdmin
                    .deletePackageSchedule(packageScheduleId);

            if (!isSuccess) {
                log.error("Failed to delete package schedule: {}", packageScheduleId);
                return new ApiResponse<>(500, "Failed to delete package schedule", null);
            }

            log.info("Successfully deleted package schedule: {}", packageScheduleId);
            return new ApiResponse<>(200, "Package schedule deleted successfully", null);

        } catch (IllegalStateException e) {
            log.error("State error: {}", e.getMessage());
            return new ApiResponse<>(409, "Error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error deleting package schedule {}: {}", packageScheduleId, e.getMessage());
            return new ApiResponse<>(500, "Failed to delete package schedule", null);
        }
    }

    /**
     * Register multiple doctors to a package schedule
     * Assigns multiple doctors to a specific health check package schedule
     * Only assigns doctors without conflicting schedules at the same time
     * 
     * @param packageScheduleId The ID of the package schedule
     * @param doctorIds List of doctor IDs to register
     * @return Success status of the registration
     */
    @PostMapping("/{packageScheduleId}/register-doctors")
    public ApiResponse<String> registerDoctorsToPackageSchedule(
            @PathVariable String packageScheduleId,
            @RequestBody List<String> doctorIds) {
        log.info("Registering {} doctors to package schedule: {}", 
                doctorIds != null ? doctorIds.size() : 0, packageScheduleId);

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        if (doctorIds == null || doctorIds.isEmpty()) {
            log.warn("No doctor IDs provided");
            return new ApiResponse<>(400, "Doctor IDs list cannot be empty", null);
        }

        try {
            boolean isSuccess = _scheduleApplicationServiceClinicAdmin
                    .registerDoctorsToPackageSchedule(packageScheduleId, doctorIds);

            if (!isSuccess) {
                log.error("Failed to register doctors to package schedule: {}", packageScheduleId);
                return new ApiResponse<>(500, "Failed to register doctors", null);
            }

            log.info("Successfully registered doctors to package schedule: {}", packageScheduleId);
            return new ApiResponse<>(200, "Doctors registered successfully", null);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return new ApiResponse<>(400, "Validation error: " + e.getMessage(), null);
        } catch (IllegalStateException e) {
            log.error("State error: {}", e.getMessage());
            return new ApiResponse<>(409, "Error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error registering doctors to package schedule {}: {}", 
                    packageScheduleId, e.getMessage());
            return new ApiResponse<>(500, "Failed to register doctors", null);
        }
    }
}
