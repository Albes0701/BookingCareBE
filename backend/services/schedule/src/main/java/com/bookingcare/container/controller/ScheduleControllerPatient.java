package com.bookingcare.container.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.QueryHealthCheckPackageSchedulesResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.ports.input.IScheduleApplicationServicePatient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/schedule")
@Slf4j
public class ScheduleControllerPatient {
    private final IScheduleApplicationServicePatient _scheduleApplicationService;

    @GetMapping(value = "test")
    public ApiResponse<String> test() {
        return new ApiResponse<>(200, "Schedule service is up and running! 2222", null);
    }

    // ==== Patient APIs ==== //
    
    /**
     * Get detailed information of a health check package schedule by ID
     * 
     * @param packageScheduleId The ID of the package schedule
     * @return QueryHealthCheckPackageSchedulesResponse containing schedule details
     */
    @GetMapping("/{packageScheduleId}")
    public ApiResponse<QueryHealthCheckPackageSchedulesResponse> getPackageScheduleById(@PathVariable String packageScheduleId) {
        log.info("Fetching package schedule for id: {}", packageScheduleId);

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        QueryHealthCheckPackageSchedulesResponse response = _scheduleApplicationService.getHealthCheckPackageScheduleById(packageScheduleId);

        return new ApiResponse<>(200, "Success", response);
    }

    /**
     * Get all package schedules by package ID
     * Returns a list of all available schedules for a specific health check package
     * 
     * @param packageId The ID of the health check package
     * @return List of QueryPackageScheduleResponse containing schedule information
     */
    @GetMapping("/by-package/{packageId}")
    public ApiResponse<List<QueryPackageScheduleResponse>> getPackageScheduleByPackageId(@PathVariable String packageId) {
        log.info("Fetching schedules for package ID: {}", packageId);

        if (packageId == null || packageId.isEmpty()) {
            log.warn("Invalid package ID provided");
            return new ApiResponse<>(400, "Invalid package ID", null);
        }

        List<QueryPackageScheduleResponse> response = _scheduleApplicationService
                .getPackageScheduleByPackageId(packageId);

        return new ApiResponse<>(200, "Success", response);
    }

    /**
     * Get package schedules by package slug and specific date
     * Useful for patients to view available schedules on a particular date
     * 
     * @param healthCheckPackageSlug The slug identifier of the health check package
     * @param scheduleDate The target date for which to retrieve schedules
     * @return List of QueryPackageScheduleResponse for the specified date
     */
    @GetMapping("/by-slug-and-date/{healthCheckPackageSlug}/{scheduleDate}")
    public ApiResponse<List<QueryPackageScheduleResponse>> getHealthCheckPackageSchedulesBySlugAndDate(
            @PathVariable String healthCheckPackageSlug, @PathVariable LocalDate scheduleDate) {
        log.info("Fetching schedules for health check package slug: {} on date: {}", healthCheckPackageSlug,
                scheduleDate);

        if (healthCheckPackageSlug == null || healthCheckPackageSlug.isEmpty()) {
            log.warn("Invalid health check package slug provided");
            return new ApiResponse<>(400, "Invalid health check package slug", null);
        }

        if (scheduleDate == null) {
            log.warn("Invalid date provided");
            return new ApiResponse<>(400, "Invalid date", null);
        }

        List<QueryPackageScheduleResponse> response = _scheduleApplicationService
                .getHealthCheckPackageSchedulesBySlugAndDate(healthCheckPackageSlug, scheduleDate);

        return new ApiResponse<>(200, "Success", response);
    }

    /**
     * Get all package schedules assigned to a specific doctor
     * Retrieves all health check package slots where a particular doctor is assigned
     * 
     * @param doctorId The ID of the doctor
     * @return List of QueryPackageScheduleResponse for schedules assigned to the doctor
     */
    @GetMapping("/by-doctor/{doctorId}")
    public ApiResponse<List<QueryPackageScheduleResponse>> getPackageScheduleByDoctorId(@PathVariable String doctorId) {
        log.info("Fetching schedules for doctor ID: {}", doctorId);

        if (doctorId == null || doctorId.isEmpty()) {
            log.warn("Invalid doctor ID provided");
            return new ApiResponse<>(400, "Invalid doctor ID", null);
        }

        List<QueryPackageScheduleResponse> response = _scheduleApplicationService
                .getPackageScheduleByDoctorId(doctorId);

        return new ApiResponse<>(200, "Success", response);
    }

    // ==== Internal APIs ==== //
    
    /**
     * [Internal API] Hold a schedule slot for a booking
     * Temporarily reserves a slot in the package schedule for a specified duration (15 minutes)
     * 
     * @param packageScheduleId The ID of the package schedule to hold
     * @param bookingId The ID of the associated booking
     * @return Schedule hold ID that can be used for confirmation or cancellation
     */
    @PostMapping("/internal/hold/{packageScheduleId}/{bookingId}")
    public ApiResponse<String> holdScheduleForBooking(@PathVariable String packageScheduleId, @PathVariable String bookingId) {
        log.info("Holding schedule for package schedule ID: {} and booking ID: {}", packageScheduleId, bookingId);

        if (packageScheduleId == null || packageScheduleId.isEmpty() || bookingId == null || bookingId.isEmpty()) {
            log.warn("Invalid package schedule ID or booking ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID or booking ID", null);
        }

        String scheduleHoldId = _scheduleApplicationService.holdScheduleForBooking(packageScheduleId, bookingId);

        return new ApiResponse<>(200, "Schedule held successfully", scheduleHoldId);
    }

    /**
     * [Internal API] Confirm a previously held schedule slot
     * Converts a HOLD status to BOOKED status and increments the booked count
     * 
     * @param scheduleHoldId The ID of the schedule hold to confirm
     * @param bookingId The ID of the associated booking (for verification)
     * @return Success status of the confirmation
     */
    @PutMapping("/internal/confirm-hold/{scheduleHoldId}/{bookingId}")
    public ApiResponse<String> confirmHoldScheduleForBooking(@PathVariable String scheduleHoldId, @PathVariable String bookingId) {
        log.info("Confirming hold for schedule hold ID: {} and booking ID: {}", scheduleHoldId, bookingId);

        if (scheduleHoldId == null || scheduleHoldId.isEmpty() || bookingId == null || bookingId.isEmpty()) {
            log.warn("Invalid schedule hold ID or booking ID provided");
            return new ApiResponse<>(400, "Invalid schedule hold ID or booking ID", null);
        }

        Boolean isSuccess = _scheduleApplicationService.confirmHoldScheduleForBooking(scheduleHoldId, bookingId);

        if (!isSuccess) {
            log.error("Failed to confirm hold for schedule hold ID: {}", scheduleHoldId);
            return new ApiResponse<>(500, "Failed to confirm hold", null);
        }

        return new ApiResponse<>(200, "Hold confirmed successfully", null);
    }

    /**
     * [Internal API] Expire a held schedule slot
     * Releases a held slot after the hold period expires or is automatically expired
     * Changes status from HOLD to RELEASED without incrementing booked count
     * 
     * @param scheduleHoldId The ID of the schedule hold to expire
     * @param bookingId The ID of the associated booking (for verification)
     * @return Success status of the expiration
     */
    @PutMapping("/internal/expire-hold/{scheduleHoldId}/{bookingId}")
    public ApiResponse<String> expiredHoldScheduleForBooking(@PathVariable String scheduleHoldId, @PathVariable String bookingId) {
        log.info("Expiring hold for schedule hold ID: {} and booking ID: {}", scheduleHoldId, bookingId);

        if (scheduleHoldId == null || scheduleHoldId.isEmpty() || bookingId == null || bookingId.isEmpty()) {
            log.warn("Invalid schedule hold ID or booking ID provided");
            return new ApiResponse<>(400, "Invalid schedule hold ID or booking ID", null);
        }

        try {
            Boolean isSuccess = _scheduleApplicationService.expiredHoldScheduleForBooking(scheduleHoldId, bookingId);

            if (!isSuccess) {
                log.error("Failed to expire hold for schedule hold ID: {}", scheduleHoldId);
                return new ApiResponse<>(500, "Failed to expire hold", null);
            }

            return new ApiResponse<>(200, "Hold expired successfully", null);
        } catch (RuntimeException e) {
            log.error("Error expiring hold: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unexpected error expiring hold: {}", e.getMessage());
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    /**
     * [Internal API] Cancel a held or booked schedule slot
     * Releases a slot by changing status to RELEASED and decrements booked count if status was BOOKED
     * Can be called for both HOLD and BOOKED statuses
     * 
     * @param scheduleHoldId The ID of the schedule hold to cancel
     * @param bookingId The ID of the associated booking (for verification)
     * @return Success status of the cancellation
     */
    @PutMapping("/internal/cancel-hold/{scheduleHoldId}/{bookingId}")
    public ApiResponse<String> cancelHoldScheduleForBooking(@PathVariable String scheduleHoldId, @PathVariable String bookingId) {
        log.info("Canceling hold for schedule hold ID: {} and booking ID: {}", scheduleHoldId, bookingId);

        if (scheduleHoldId == null || scheduleHoldId.isEmpty() || bookingId == null || bookingId.isEmpty()) {
            log.warn("Invalid schedule hold ID or booking ID provided");
            return new ApiResponse<>(400, "Invalid schedule hold ID or booking ID", null);
        }

        Boolean isSuccess = _scheduleApplicationService.cancelHoldScheduleForBooking(scheduleHoldId, bookingId);

        if (!isSuccess) {
            log.error("Failed to cancel hold for schedule hold ID: {}", scheduleHoldId);
            return new ApiResponse<>(500, "Failed to cancel hold", null);
        }

        return new ApiResponse<>(200, "Hold cancelled successfully", null);
    }

    /**
     * [Internal API] Check if a schedule slot is available for booking
     * Verifies whether a package schedule slot has available capacity considering overbooking limits
     * 
     * @param packageScheduleId The ID of the package schedule to check
     * @return Boolean indicating availability (true if slots are available, false otherwise)
     */
    @GetMapping("/internal/check-availability/{packageScheduleId}")
    public ApiResponse<Boolean> isScheduleAvailable(@PathVariable String packageScheduleId) {
        log.info("Checking availability for package schedule ID: {}", packageScheduleId);

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        Boolean isAvailable = _scheduleApplicationService.isScheduleAvailable(packageScheduleId);

        return new ApiResponse<>(200, "Success", isAvailable);
    }
}
