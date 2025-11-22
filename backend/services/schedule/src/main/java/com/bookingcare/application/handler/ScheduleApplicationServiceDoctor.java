package com.bookingcare.application.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.mapper.ScheduleApplicationMapper;
import com.bookingcare.application.ports.input.IScheduleApplicationServiceDoctor;
import com.bookingcare.application.ports.output.IHealthCheckPackageDoctorSchedulesRepository;
import com.bookingcare.application.ports.output.IHealthCheckPackageSchedulesRepository;
import com.bookingcare.application.ports.output.IScheduleRepository;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleDoctor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleApplicationServiceDoctor implements IScheduleApplicationServiceDoctor{

    private final IHealthCheckPackageSchedulesRepository _healthCheckPackageSchedulesRepository;
    private final IHealthCheckPackageDoctorSchedulesRepository _healthCheckPackageDoctorSchedulesRepository;
    private final IScheduleRepository _scheduleRepository;
    private final ScheduleApplicationMapper scheduleMapper;

    @Override
    @Transactional
    public boolean registerDoctorSchedules(String doctorId, List<String> PackageScheduleIds) {
        try {
            // 1. Validate input parameters
            if (doctorId == null || doctorId.isEmpty()) {
                log.warn("Invalid doctorId provided");
                throw new IllegalArgumentException("Doctor ID cannot be null or empty");
            }
            
            if (PackageScheduleIds == null || PackageScheduleIds.isEmpty()) {
                log.warn("No schedule IDs provided for doctor: {}", doctorId);
                throw new IllegalArgumentException("Schedule IDs list cannot be null or empty");
            }

            // 2. Verify all schedules exist
            for (String packageScheduleId : PackageScheduleIds) {
                var packageSchedule = _healthCheckPackageSchedulesRepository.findById(packageScheduleId)
                        .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
                
                // Optional: Validate schedule is not already fully assigned (if needed)
                log.debug("Found schedule: {} for package: {}", packageScheduleId, packageSchedule.getPackageId());
            }

            // 3. Register schedules to doctor
            for (String packageScheduleId : PackageScheduleIds) {
                // Check if doctor is already assigned to this schedule
                var existingAssignment = _healthCheckPackageDoctorSchedulesRepository
                        .findByPackageScheduleIdAndDoctorId(packageScheduleId, doctorId);
                
                if (existingAssignment.isPresent() && !existingAssignment.get().getIsDeleted()) {
                    log.warn("Doctor {} is already assigned to schedule {}", doctorId, packageScheduleId);
                    continue; // Skip if already assigned and not deleted
                }

                // Create new assignment
                var doctorSchedule = HealthCheckPackageScheduleDoctor.builder()
                        .id(generateId())
                        .doctorId(doctorId)
                        .packageScheduleId(packageScheduleId)
                        .isDeleted(false)
                        .build();

                _healthCheckPackageDoctorSchedulesRepository.save(doctorSchedule);
                log.info("Assigned doctor {} to schedule {}", doctorId, packageScheduleId);
            }

            log.info("Successfully registered doctor {} to {} schedules", doctorId, PackageScheduleIds.size());
            return true;

        } catch (IllegalArgumentException e) {
            log.error("Validation error while registering doctor schedules: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering doctor {} to schedules: {}", doctorId, e.getMessage());
            throw new RuntimeException("Failed to register doctor schedules", e);
        }
    }

    // Helper method to generate unique ID (you can use your own ID generation logic)
    private String generateId() {
        return "HPSD" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    @Override
    public List<QueryPackageScheduleResponse> getAllSchedulesByDoctorId(String doctorId) {
        try {
            // Lấy danh sách HealthCheckPackageScheduleDoctor theo doctorId
            var doctorSchedules = _healthCheckPackageDoctorSchedulesRepository
                    .findByDoctorId(doctorId);
            
            if (doctorSchedules == null || doctorSchedules.isEmpty()) {
                log.warn("No package schedules found for doctor: {}", doctorId);
                return new ArrayList<>();
            }
            
            // Lấy thông tin chi tiết từ packageScheduleId
            return doctorSchedules.stream()
                    .map(doctorSchedule -> {
                        // Fetch HealthCheckPackageSchedule object từ packageScheduleId
                        var packageSchedule = _healthCheckPackageSchedulesRepository
                                .findById(doctorSchedule.getPackageScheduleId());
                        
                        if (packageSchedule.isEmpty()) {
                            log.warn("Package schedule not found: {}", doctorSchedule.getPackageScheduleId());
                            return null;
                        }
                        
                        // Fetch Schedule object từ scheduleId
                        var schedule = _scheduleRepository
                                .findById(packageSchedule.get().getScheduleId());
                        
                        return scheduleMapper.toQueryPackageScheduleResponse(
                                packageSchedule.get(),
                                schedule.orElse(null)
                        );
                    })
                    .filter(response -> response != null)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching package schedules for doctor: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteSchedulePackageDoctorById(String doctorId, String packageScheduleId) {
        try {
            // Validate input parameters
            if (doctorId == null || doctorId.isEmpty()) {
                log.warn("Invalid doctorId provided");
                throw new IllegalArgumentException("Doctor ID cannot be null or empty");
            }
            
            if (packageScheduleId == null || packageScheduleId.isEmpty()) {
                log.warn("Invalid packageScheduleId provided");
                throw new IllegalArgumentException("Package Schedule ID cannot be null or empty");
            }
            
            // Find existing assignment
            var existingAssignment = _healthCheckPackageDoctorSchedulesRepository
                    .findByPackageScheduleIdAndDoctorId(packageScheduleId, doctorId);
            
            if (existingAssignment.isEmpty()) {
                log.warn("No assignment found for doctor {} and schedule {}", doctorId, packageScheduleId);
                return false;
            }
            
            // Hard delete - remove record from database
            var assignment = existingAssignment.get();
            _healthCheckPackageDoctorSchedulesRepository.deleteById(assignment.getId());
            
            log.info("Hard deleted assignment for doctor {} and schedule {}", doctorId, packageScheduleId);
            return true;
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deleting assignment for doctor {} and schedule {}: {}", 
                    doctorId, packageScheduleId, e.getMessage());
            throw new RuntimeException("Failed to delete doctor schedule assignment", e);
        }
    }

}
