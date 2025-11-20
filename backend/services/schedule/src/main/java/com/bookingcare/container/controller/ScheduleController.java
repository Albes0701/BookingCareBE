package com.bookingcare.container.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.QueryHealthCheckPackageSchedulesResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.dto.UpdateHealthCheckPackageSchedulesCommand;
import com.bookingcare.application.ports.input.IScheduleApplicationServicePatient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/schedule")
@Slf4j
public class ScheduleController {
    private final IScheduleApplicationServicePatient _scheduleApplicationService;

    @GetMapping(value = "test")
    public ApiResponse<String> test() {
        return new ApiResponse<>(200, "Schedule service is up and running! 2222", null);
    }

    @GetMapping("/{packageScheduleId}")
    public ApiResponse<QueryPackageScheduleResponse> getPackageScheduleById(@PathVariable String packageScheduleId) {
        log.info("Fetching package schedule for id: {}", packageScheduleId);

        if (packageScheduleId == null || packageScheduleId.isEmpty()) {
            log.warn("Invalid package schedule ID provided");
            return new ApiResponse<>(400, "Invalid package schedule ID", null);
        }

        QueryPackageScheduleResponse response = _scheduleApplicationService.getPackageScheduleById(packageScheduleId);

        return new ApiResponse<>(200, "Success", response);
    }

    @GetMapping("/get-health-check-package-schedules-by-date/{healthCheckPackageSlug}/{scheduleDate}")
    public ApiResponse<List<QueryHealthCheckPackageSchedulesResponse>> getHealthCheckPackageSchedulesByDate(
            @PathVariable String healthCheckPackageSlug, @PathVariable LocalDate scheduleDate) {
        log.info("Fetching schedules for health check package ID: {} on date: {}", healthCheckPackageSlug,
                scheduleDate);

        if (healthCheckPackageSlug == null || healthCheckPackageSlug.isEmpty()) {
            log.warn("Invalid health check package ID or date provided");
            return new ApiResponse<>(400, "Invalid health check package ID", null);
        }

        if (scheduleDate == null) {
            log.warn("Invalid health check package ID or date provided");
            return new ApiResponse<>(400, "Invalid date", null);
        }

        List<QueryHealthCheckPackageSchedulesResponse> response = _scheduleApplicationService
                .getHealthCheckPackageSchedulesByDate(healthCheckPackageSlug, scheduleDate);

        return new ApiResponse<>(200, "Success", response);
    }

    @PatchMapping("/update-health-check-package-schedules")
    public ApiResponse<String> updateHealthCheckPackageSchedules(@RequestBody UpdateHealthCheckPackageSchedulesCommand command) {
        log.info("Updating health check package schedules");

        if (command == null || command.schedules().isEmpty()) {
            log.warn("No update commands provided");
            return new ApiResponse<>(400, "No update commands provided", null);
        }
        
        Boolean isSuccess = _scheduleApplicationService.updateHealthCheckPackageSchedules(command);

        if (!isSuccess) {
            log.error("Failed to update health check package schedules");
            return new ApiResponse<>(500, "Failed to update health check package schedules", null);
        }

        return new ApiResponse<>(200, "Health check package schedules updated successfully", null);
    }
}
