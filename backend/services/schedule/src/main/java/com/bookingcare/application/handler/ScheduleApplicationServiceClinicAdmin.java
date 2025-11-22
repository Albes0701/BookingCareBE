package com.bookingcare.application.handler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bookingcare.application.dto.PackageScheduleRequest;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.mapper.ScheduleApplicationMapper;
import com.bookingcare.application.ports.input.IScheduleApplicationServiceClinicAdmin;
import com.bookingcare.application.ports.output.IHealthCheckPackageDoctorSchedulesRepository;
import com.bookingcare.application.ports.output.IHealthCheckPackageSchedulesRepository;
import com.bookingcare.application.ports.output.IScheduleRepository;
import com.bookingcare.domain.entity.HealthCheckPackageSchedule;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleDoctor;
import com.bookingcare.infrastructure.client.ClinicBranchPackageClient;
import com.bookingcare.infrastructure.client.dto.ClinicBranchPackageResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleApplicationServiceClinicAdmin implements IScheduleApplicationServiceClinicAdmin {

    private final IHealthCheckPackageSchedulesRepository _healthCheckPackageSchedulesRepository;
    private final IHealthCheckPackageDoctorSchedulesRepository _healthCheckPackageDoctorSchedulesRepository;
    private final IScheduleRepository _scheduleRepository;
    private final ScheduleApplicationMapper scheduleMapper;
    private final ClinicBranchPackageClient clinicBranchPackageClient;

    

    @Override
    public List<QueryPackageScheduleResponse> getAllSchedulesByClinicBranchId(String clinicBranchId) {
        try {
            // 1. Validate input parameter
            if (clinicBranchId == null || clinicBranchId.isEmpty()) {
                log.warn("Invalid clinicBranchId provided");
                throw new IllegalArgumentException("Clinic Branch ID cannot be null or empty");
            }
            
            clinicBranchId = clinicBranchId.trim();
            
            // 2. Fetch clinic branch details using OpenFeign
            log.debug("Fetching clinic branch details for ID: {}", clinicBranchId);
            
            List<ClinicBranchPackageResponse> clinicBranchPackages = clinicBranchPackageClient.getClinicBranchesByPackageId(clinicBranchId);
            
            if (clinicBranchPackages == null || clinicBranchPackages.isEmpty()) {
                log.warn("Clinic branch not found: {}", clinicBranchId);
                return new ArrayList<>();
            }
            
            
            // 3. Fetch health check packages for the clinic branch using OpenFeign
            log.debug("Fetching health check packages for clinic branch: {}", clinicBranchId);
            
            List<ClinicBranchPackageResponse> packages = clinicBranchPackageClient.getClinicBranchesByPackageId(clinicBranchId);
            
            if (packages == null || packages.isEmpty()) {
                log.info("No health check packages found for clinic branch: {}", clinicBranchId);
                return new ArrayList<>();
            }
            
            log.debug("Found {} health check packages for clinic branch: {}", 
                    packages.size(), clinicBranchId);
            
            // 4. Extract package IDs from the response
            List<String> packageIds = packages.stream()
                    .map(ClinicBranchPackageResponse::getHealthcheckPackageId)
                    .filter(id -> id != null && !id.isEmpty())
                    .toList();
            
            log.debug("Extracted {} package IDs from clinic branch packages", packageIds.size());
            
            if (packageIds.isEmpty()) {
                log.info("No valid package IDs found for clinic branch: {}", clinicBranchId);
                return new ArrayList<>();
            }
            
            // 5. Fetch all schedules for these packages
            List<QueryPackageScheduleResponse> allSchedules = new ArrayList<>();
            
            for (String packageId : packageIds) {
                try {
                    log.debug("Fetching schedules for package ID: {}", packageId);
                    
                    var packageSchedules = _healthCheckPackageSchedulesRepository
                            .findByPackageIdAndNotDeleted(packageId);
                    
                    if (packageSchedules == null || packageSchedules.isEmpty()) {
                        log.debug("No schedules found for package: {}", packageId);
                        continue;
                    }
                    
                    // 6. Map package schedules to response DTOs
                    var scheduleResponses = packageSchedules.stream()
                            .map(packageSchedule -> {
                                try {
                                    var schedule = _scheduleRepository.findById(packageSchedule.getScheduleId())
                                            .orElse(null);
                                    
                                    if (schedule == null) {
                                        log.warn("Schedule not found for package schedule: {}", 
                                                packageSchedule.getPackageScheduleId());
                                        return null;
                                    }
                                    
                                    return scheduleMapper.toQueryPackageScheduleResponse(packageSchedule, schedule);
                                } catch (Exception e) {
                                    log.error("Error mapping package schedule {}: {}", 
                                            packageSchedule.getPackageScheduleId(), e.getMessage());
                                    return null;
                                }
                            })
                            .filter(response -> response != null)
                            .toList();
                    
                    allSchedules.addAll(scheduleResponses);
                    log.debug("Added {} schedules for package: {}", scheduleResponses.size(), packageId);
                    
                } catch (Exception e) {
                    log.error("Error fetching schedules for package {}: {}", packageId, e.getMessage());
                    // Continue with next package instead of failing
                    continue;
                }
            }
            
            log.info("Successfully fetched {} total schedules for clinic branch: {}", 
                    allSchedules.size(), clinicBranchId);
            
            return allSchedules;
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching schedules for clinic branch {}: {}", 
                    clinicBranchId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch schedules for clinic branch", e);
        }
    }

    @Override
    @Transactional
    public QueryPackageScheduleResponse createPackageSchedule(PackageScheduleRequest request) {
        try {   
            // 1. Verify schedule exists
            var schedule = _scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found: " + request.scheduleId()));
            
            log.debug("Found schedule: {} for date: {}", request.scheduleId(), request.date());
            
            // 2. Check if package schedule already exists
            var existingPackageSchedule = _healthCheckPackageSchedulesRepository
                    .findByHealthCheckPackageIdScheduleIdAndDate(
                            request.packageId(),
                            request.scheduleId(),
                            request.getScheduleDate());
            
            if (existingPackageSchedule.isPresent() && !existingPackageSchedule.get().getIsDeleted()) {
                log.warn("Package schedule already exists for package: {}, schedule: {}, date: {}",
                        request.packageId(), request.scheduleId(), request.getScheduleDate());
                throw new RuntimeException("Package schedule already exists for this date");
            }
            
            // 3. Generate unique package schedule ID using hash
            String rawPackageScheduleId = request.packageId().substring(6)
                    + request.scheduleId().substring(6).replace("_", "")
                    + request.getScheduleDate().toString().replace("-", "");
            
            String encryptedPackageScheduleId = hashToSha256AndTruncate(rawPackageScheduleId);
            
            log.debug("Generated package schedule ID: {}", encryptedPackageScheduleId);
            
            // 4. Create new package schedule
            var newPackageSchedule = HealthCheckPackageSchedule.builder()
                    .packageScheduleId(encryptedPackageScheduleId)
                    .packageId(request.packageId())
                    .scheduleId(request.scheduleId())
                    .scheduleDate(request.getScheduleDate())
                    .capacity(request.capacity())
                    .bookedCount(0) // Initialize booked count
                    .overbookLimit(0) // Initialize overbook limit
                    .isDeleted(false)
                    .createdAt(ZonedDateTime.now())
                    .build();
            
            // 5. Save to database
            var savedPackageSchedule = _healthCheckPackageSchedulesRepository.save(newPackageSchedule);
            
            log.info("Successfully created package schedule: {} for package: {}, schedule: {}, date: {}",
                    encryptedPackageScheduleId, request.packageId(), request.scheduleId(), 
                    request.getScheduleDate());
            
            // 7. Map to response DTO
            return scheduleMapper.toQueryPackageScheduleResponse(savedPackageSchedule, schedule);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error while creating package schedule: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating package schedule: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create package schedule", e);
        }
    }

    @Override
    @Transactional
    public QueryPackageScheduleResponse updatePackageSchedule(String packageScheduleId, PackageScheduleRequest request) {
        try {
            // 1. Find existing package schedule
            var existingPackageSchedule = _healthCheckPackageSchedulesRepository.findById(packageScheduleId)
                    .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
            
            log.debug("Found existing package schedule: {}", packageScheduleId);
            
            // 2. Verify schedule exists
            var schedule = _scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found: " + request.scheduleId()));
            
            log.debug("Found schedule: {} for date: {}", request.scheduleId(), request.date());
            
            // 3. Check if booked count exceeds new capacity (prevent reducing capacity below booked count)
            if (existingPackageSchedule.getBookedCount() > request.capacity()) {
                log.warn("Cannot reduce capacity below booked count. Current booked: {}, New capacity: {}",
                        existingPackageSchedule.getBookedCount(), request.capacity());
                throw new IllegalArgumentException("Capacity cannot be less than the current booked count (" 
                        + existingPackageSchedule.getBookedCount() + ")");
            }
            
            // 4. Check if updating date creates duplicate (if date is being changed)
            if (!existingPackageSchedule.getScheduleDate().equals(request.getScheduleDate())) {
                var duplicatePackageSchedule = _healthCheckPackageSchedulesRepository
                        .findByHealthCheckPackageIdScheduleIdAndDate(
                                request.packageId(),
                                request.scheduleId(),
                                request.getScheduleDate());
                
                if (duplicatePackageSchedule.isPresent() && !duplicatePackageSchedule.get().getIsDeleted()) {
                    log.warn("Package schedule already exists for package: {}, schedule: {}, date: {}",
                            request.packageId(), request.scheduleId(), request.getScheduleDate());
                    throw new RuntimeException("Package schedule already exists for this date");
                }
            }
            
            // 5. Update fields
            existingPackageSchedule.setPackageId(request.packageId());
            existingPackageSchedule.setScheduleId(request.scheduleId());
            existingPackageSchedule.setScheduleDate(request.getScheduleDate());
            existingPackageSchedule.setCapacity(request.capacity());
            existingPackageSchedule.setUpdatedAt(ZonedDateTime.now());
            
            log.debug("Updated package schedule fields - PackageId: {}, ScheduleId: {}, Date: {}, Capacity: {}",
                    request.packageId(), request.scheduleId(), request.getScheduleDate(), request.capacity());
            
            // 6. Save updated package schedule
            var updatedPackageSchedule = _healthCheckPackageSchedulesRepository.save(existingPackageSchedule);
            
            log.info("Successfully updated package schedule: {} for package: {}, schedule: {}, date: {}, capacity: {}",
                    packageScheduleId, request.packageId(), request.scheduleId(), 
                    request.getScheduleDate(), request.capacity());
            
            // 7. Map to response DTO
            return scheduleMapper.toQueryPackageScheduleResponse(updatedPackageSchedule, schedule);
        
        } catch (IllegalArgumentException e) {
            log.error("Validation error while updating package schedule: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating package schedule {}: {}", packageScheduleId, e.getMessage(), e);
            throw new RuntimeException("Failed to update package schedule", e);
        }
    }

    @Override
    @Transactional
    public boolean deletePackageSchedule(String packageScheduleId) {
        try {
            // 1. Validate input parameters
            if (packageScheduleId == null || packageScheduleId.isEmpty()) {
                log.warn("Invalid packageScheduleId provided");
                throw new IllegalArgumentException("Package Schedule ID cannot be null or empty");
            }
            
            // 2. Find existing package schedule
            var existingPackageSchedule = _healthCheckPackageSchedulesRepository.findById(packageScheduleId)
                    .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
            
            log.debug("Found package schedule to delete: {}", packageScheduleId);
            
            
            // 3. Check if there are active bookings/holds on this schedule
            if (existingPackageSchedule.getBookedCount() > 0) {
                log.warn("Cannot delete package schedule {} - has {} active bookings",
                        packageScheduleId, existingPackageSchedule.getBookedCount());
                throw new IllegalStateException("Cannot delete package schedule with active bookings. "
                        + "Please cancel all bookings first. Current bookings: " 
                        + existingPackageSchedule.getBookedCount());
            }
            
            // 4. Delete associated doctor assignments (hard delete)
            var doctorAssignments = _healthCheckPackageDoctorSchedulesRepository
                    .findByPackageScheduleId(packageScheduleId);

            for (var assignment : doctorAssignments) {
                _healthCheckPackageDoctorSchedulesRepository.deleteById(assignment.getId());
                log.debug("Hard deleted doctor assignment: {} for schedule: {}", 
                        assignment.getId(), packageScheduleId);
            }
            
            // 5. Soft delete the package schedule
            existingPackageSchedule.setIsDeleted(true);
            existingPackageSchedule.setUpdatedAt(ZonedDateTime.now());
            
            _healthCheckPackageSchedulesRepository.save(existingPackageSchedule);
            
            log.info("Successfully deleted package schedule: {}", packageScheduleId);
            return true;
            
        } catch (IllegalStateException e) {
            log.error("State error while deleting package schedule: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Validation error while deleting package schedule: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deleting package schedule {}: {}", packageScheduleId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete package schedule", e);
        }
    }


    private String hashToSha256AndTruncate(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert hash bytes to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Return first 16 characters of the hash
            return hexString.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            // Fallback to a simple hash if SHA-256 is not available
            return String.valueOf(input.hashCode()).replace("-", "").substring(0, Math.min(16, input.length()));
        }
    }

    @Override
    @Transactional
    public boolean registerDoctorsToPackageSchedule(String packageScheduleId, List<String> doctorIds) {
        try {
            // 1. Validate input parameters
            if (packageScheduleId == null || packageScheduleId.isEmpty()) {
                log.warn("Invalid packageScheduleId provided");
                throw new IllegalArgumentException("Package Schedule ID cannot be null or empty");
            }
            
            if (doctorIds == null || doctorIds.isEmpty()) {
                log.warn("No doctor IDs provided for package schedule: {}", packageScheduleId);
                throw new IllegalArgumentException("Doctor IDs list cannot be null or empty");
            }
            
            // 2. Find existing package schedule
            var packageSchedule = _healthCheckPackageSchedulesRepository.findById(packageScheduleId)
                    .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
            
            log.debug("Found package schedule: {}", packageScheduleId);
            
            // 3. Check if package schedule is deleted
            if (packageSchedule.getIsDeleted()) {
                log.warn("Cannot register doctors to deleted package schedule: {}", packageScheduleId);
                throw new IllegalStateException("Package schedule is deleted");
            }
            
            // 4. Get schedule time details
            var schedule = _scheduleRepository.findById(packageSchedule.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found: " + packageSchedule.getScheduleId()));
            
            log.debug("Found schedule details for package schedule: {}", packageScheduleId);
            
            int successfulRegistrations = 0;
            int skippedRegistrations = 0;
            List<String> rejectedDoctors = new ArrayList<>();
            
            // 5. Register each doctor
            for (String doctorId : doctorIds) {
                try {
                    // Validate doctor ID
                    if (doctorId == null || doctorId.isEmpty()) {
                        log.warn("Skipping null or empty doctor ID");
                        skippedRegistrations++;
                        continue;
                    }
                    
                    doctorId = doctorId.trim();
                    
                    // 5a. Check if doctor is already assigned to this package schedule
                    var existingAssignment = _healthCheckPackageDoctorSchedulesRepository
                            .findByPackageScheduleIdAndDoctorId(packageScheduleId, doctorId);
                    
                    if (existingAssignment.isPresent() && !existingAssignment.get().getIsDeleted()) {
                        log.warn("Doctor {} is already assigned to package schedule {}", doctorId, packageScheduleId);
                        skippedRegistrations++;
                        continue;
                    }
                    
                    // 5b. Check for schedule conflicts - doctor cannot have another schedule at same time
                    var conflictingAssignments = _healthCheckPackageDoctorSchedulesRepository
                            .findByDoctorIdAndScheduleDateAndTimeSlot(
                                    doctorId,
                                    packageSchedule.getScheduleDate(),
                                    schedule.getStartTime(),
                                    schedule.getEndTime());
                    
                    if (!conflictingAssignments.isEmpty()) {
                        log.warn("Doctor {} has conflicting schedule on {} from {} to {}",
                                doctorId, 
                                packageSchedule.getScheduleDate(),
                                schedule.getStartTime(),
                                schedule.getEndTime());
                        rejectedDoctors.add(doctorId);
                        skippedRegistrations++;
                        continue;
                    }
                    
                    // 5c. Create doctor schedule assignment
                    var doctorScheduleAssignment = HealthCheckPackageScheduleDoctor.builder()
                            .id(generateId()) // Generate unique ID (e.g., "HPSD" + UUID)
                            .packageScheduleId(packageScheduleId)
                            .doctorId(doctorId)
                            .isDeleted(false)
                            .createdAt(ZonedDateTime.now())
                            .build();
                    
                    _healthCheckPackageDoctorSchedulesRepository.save(doctorScheduleAssignment);
                    
                    log.info("Successfully registered doctor {} to package schedule {}", doctorId, packageScheduleId);
                    successfulRegistrations++;
                    
                } catch (Exception e) {
                    log.error("Error registering doctor {} to package schedule {}: {}", 
                            doctorId, packageScheduleId, e.getMessage());
                    skippedRegistrations++;
                }
            }
            
            // 6. Log summary
            log.info("Package schedule {} registration summary - Successful: {}, Skipped: {}, Rejected (conflict): {}",
                    packageScheduleId, successfulRegistrations, skippedRegistrations, rejectedDoctors.size());
            
            if (!rejectedDoctors.isEmpty()) {
                log.warn("Doctors with schedule conflicts: {}", rejectedDoctors);
            }
            
            // 7. Return true if at least one doctor was successfully registered
            return successfulRegistrations > 0;
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error while registering doctors: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.error("State error while registering doctors: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering doctors to package schedule {}: {}", packageScheduleId, e.getMessage(), e);
            throw new RuntimeException("Failed to register doctors to package schedule", e);
        }
    }

    // Helper method to generate unique ID
    private String generateId() {
        return "HPSD" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    
}
