package com.bookingcare.application.handler;


import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.bookingcare.application.dto.PackageDetailResponse;
import com.bookingcare.application.dto.QueryHealthCheckPackageSchedulesResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.mapper.ScheduleApplicationMapper;
import com.bookingcare.application.ports.input.IScheduleApplicationServicePatient;
import com.bookingcare.application.ports.output.IHealthCheckPackageSchedulesRepository;
import com.bookingcare.application.ports.output.IHealthPackageServicePort;
import com.bookingcare.application.ports.output.IScheduleHoldRepository;
import com.bookingcare.application.ports.output.IScheduleRepository;
import com.bookingcare.domain.entity.ScheduleHold;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleApplicationServicePatient implements IScheduleApplicationServicePatient {
    private final IHealthCheckPackageSchedulesRepository _healthCheckPackageSchedulesRepository;
    private final IScheduleRepository _scheduleRepository;
    private final ScheduleApplicationMapper scheduleMapper;
    private final IHealthPackageServicePort _healthPackageServicePort;
    private final IScheduleHoldRepository _scheduleHoldRepository;

    @Override
    public List<QueryPackageScheduleResponse> getPackageScheduleByPackageId(String packageId) {
        try {
            // Implement the logic to fetch the package schedule by ID

            return _healthCheckPackageSchedulesRepository
                    .findByPackageId(packageId)
                    .stream()
                    .map(healthCheckPackageSchedule -> {
                        // Fetch Schedule object từ scheduleId
                        var schedule = _scheduleRepository.findById(healthCheckPackageSchedule.getScheduleId());
                        return scheduleMapper.toQueryPackageScheduleResponse(
                                healthCheckPackageSchedule,
                                schedule.orElse(null)
                        );
                    })
                    .toList();
        } catch (Exception e) {
            // Log the exception (logging framework assumed to be set up)
            log.error("Error fetching package schedule: " + e.getMessage());
            throw e;
        }

    }


    @Override
    public QueryHealthCheckPackageSchedulesResponse getHealthCheckPackageScheduleById(String packageScheduleId) {
        try {
            // Implement the logic to fetch the package schedule by ID

            return _healthCheckPackageSchedulesRepository
                    .findById(packageScheduleId)
                    .map(scheduleMapper::toQueryHealthCheckPackageSchedulesResponse)
                    .orElse(null);
        } catch (Exception e) {
            // Log the exception (logging framework assumed to be set up)
            log.error("Error fetching package schedule: " + e.getMessage());
            throw e;
        }
    }


    @Override
    public List<QueryPackageScheduleResponse> getHealthCheckPackageSchedulesBySlugAndDate(
            String healthCheckPackageSlug,
            LocalDate date) {
        try {
            // Gọi Package service để lấy package detail từ slug
            PackageDetailResponse packageDetailResponse = _healthPackageServicePort
                    .getPackageDetailBySlug(healthCheckPackageSlug);
            
            if (packageDetailResponse == null) {
                log.warn("Package not found for slug: " + healthCheckPackageSlug);
                return new ArrayList<>();
            }

            // Lấy danh sách schedule theo packageId và date
            return _healthCheckPackageSchedulesRepository
                    .findByPackageIdAndScheduleDate(packageDetailResponse.id().toString(), date)
                    .stream()
                    .map(healthCheckPackageSchedule -> {
                        // Fetch Schedule object từ scheduleId
                        var schedule = _scheduleRepository.findById(healthCheckPackageSchedule.getScheduleId());
                        return scheduleMapper.toQueryPackageScheduleResponse(
                                healthCheckPackageSchedule,
                                schedule.orElse(null)
                        );
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching health check package schedules: " + e.getMessage());
            throw e;
        }
    }

    


    

    @Transactional
    @Override
    public String holdScheduleForBooking(String packageScheduleId, String bookingId) {
    try {
            // 1. Kiểm tra slot còn chỗ trống không
            var packageSchedule = _healthCheckPackageSchedulesRepository
                    .findById(packageScheduleId)
                    .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
            
            // Kiểm tra capacity
            int availableSlots = packageSchedule.getCapacity() - packageSchedule.getBookedCount();
            if (availableSlots <= 0) {
                throw new RuntimeException("No available slots for package schedule: " + packageScheduleId);
            }
            
            // 2. Tạo ScheduleHold record
            String scheduleHoldId = generateScheduleHoldId(); // H + timestamp hoặc UUID
            ScheduleHold scheduleHold = ScheduleHold.builder()
                    .id(scheduleHoldId)
                    .packageScheduleId(packageScheduleId)
                    .bookingId(bookingId)
                    .status("HOLD")
                    .expireAt(ZonedDateTime.now().plusMinutes(15)) // Giữ chỗ 15 phút
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();
            
            // 3. Lưu vào database
            _scheduleHoldRepository.save(scheduleHold);
            
            log.info("Hold schedule created: {} for booking: {}", scheduleHoldId, bookingId);
            return scheduleHoldId;
        
        } catch (Exception e) {
            log.error("Error holding schedule: " + e.getMessage());
            throw e;
        }
    }

    // Helper method để generate scheduleHoldId
    private String generateScheduleHoldId() {
        return "H" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    @Override
    public Boolean confirmHoldScheduleForBooking(String scheduleHoldId, String bookingId) {
        try {
            // 1. Kiểm tra ScheduleHold tồn tại và trạng thái
            var scheduleHold = _scheduleHoldRepository
                    .findById(scheduleHoldId)
                    .orElseThrow(() -> new RuntimeException("Schedule hold not found: " + scheduleHoldId));
            
            // Kiểm tra bookingId khớp
            if (!scheduleHold.getBookingId().equals(bookingId)) {
                throw new RuntimeException("BookingId mismatch for hold: " + scheduleHoldId);
            }
            
            // Kiểm tra trạng thái là HOLD
            if (!"HOLD".equals(scheduleHold.getStatus())) {
                throw new RuntimeException("Hold status is not HOLD: " + scheduleHoldId);
            }
            
            // Kiểm tra hold chưa expire
            if (ZonedDateTime.now().isAfter(scheduleHold.getExpireAt())) {
                throw new RuntimeException("Hold has expired: " + scheduleHoldId);
            }
            
            // 2. Cập nhật ScheduleHold status thành BOOKED
            scheduleHold.setStatus("BOOKED");
            scheduleHold.setUpdatedAt(ZonedDateTime.now());
            _scheduleHoldRepository.save(scheduleHold);
            
            // 3. Cập nhật bookedCount trong HealthCheckPackageSchedule
            var packageSchedule = _healthCheckPackageSchedulesRepository
                    .findById(scheduleHold.getPackageScheduleId())
                    .orElseThrow(() -> new RuntimeException("Package schedule not found: " + scheduleHold.getPackageScheduleId()));
            
            packageSchedule.setBookedCount(packageSchedule.getBookedCount() + 1);
            packageSchedule.setUpdatedAt(ZonedDateTime.now());
            _healthCheckPackageSchedulesRepository.save(packageSchedule);
            
            log.info("Hold schedule confirmed: {} for booking: {}", scheduleHoldId, bookingId);
            return true;
            
        } catch (Exception e) {
            log.error("Error confirming hold schedule: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public Boolean expiredHoldScheduleForBooking(String scheduleHoldId, String bookingId) {
        try {
            // 1. Kiểm tra ScheduleHold tồn tại
            var scheduleHold = _scheduleHoldRepository
                    .findById(scheduleHoldId)
                    .orElseThrow(() -> new RuntimeException("Schedule hold not found: " + scheduleHoldId));
            
            // Kiểm tra bookingId khớp
            if (!scheduleHold.getBookingId().equals(bookingId)) {
                throw new RuntimeException("BookingId mismatch for hold: " + scheduleHoldId);
            }
            
            // Kiểm tra trạng thái là HOLD (chưa confirm)
            if (!"HOLD".equals(scheduleHold.getStatus())) {
                throw new RuntimeException("Cannot cancel hold with status: " + scheduleHold.getStatus());
            }
            
            // 2. Cập nhật ScheduleHold status thành RELEASED
            scheduleHold.setStatus("RELEASED");
            scheduleHold.setUpdatedAt(ZonedDateTime.now());
            _scheduleHoldRepository.save(scheduleHold);
            
            log.info("Hold schedule cancelled: {} for booking: {}", scheduleHoldId, bookingId);
            return true;
            
        } catch (Exception e) {
            log.error("Error cancelling hold schedule: " + e.getMessage());
            throw e;
        }
    }



    @Override
    public Boolean isScheduleAvailable(String packageScheduleId) {
        try {
            // 1. Kiểm tra slot tồn tại
            var packageSchedule = _healthCheckPackageSchedulesRepository
                    .findById(packageScheduleId)
                    .orElseThrow(() -> new RuntimeException("Package schedule not found: " + packageScheduleId));
            
            // 2. Kiểm tra slot đã bị xóa không
            if (packageSchedule.getIsDeleted()) {
                log.warn("Package schedule is deleted: {}", packageScheduleId);
                return false;
            }
            
            // 3. Tính số chỗ còn trống
            int availableSlots = packageSchedule.getCapacity() - packageSchedule.getBookedCount();
            
            // 4. Kiểm tra còn chỗ trống hoặc có thể overbook
            if (availableSlots > 0) {
                log.info("Schedule available: {} (available slots: {})", packageScheduleId, availableSlots);
                return true;
            }
            
            // 5. Kiểm tra xem có thể overbook không
            int totalCapacityWithOverbook = packageSchedule.getCapacity() + packageSchedule.getOverbookLimit();
            if (packageSchedule.getBookedCount() < totalCapacityWithOverbook) {
                log.info("Schedule available with overbook: {} (booked: {}, capacity with overbook: {})", 
                        packageScheduleId, packageSchedule.getBookedCount(), totalCapacityWithOverbook);
                return true;
            }
            
            // 6. Hết chỗ, không thể overbook
            log.warn("Schedule is full with no overbook available: {} (booked: {}, total capacity: {})", 
                    packageScheduleId, packageSchedule.getBookedCount(), totalCapacityWithOverbook);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking schedule availability: " + e.getMessage());
            throw e;
        }
    }


    @Transactional
    @Override
    public Boolean cancelHoldScheduleForBooking(String scheduleHoldId, String bookingId) {
        try {
            // 1. Kiểm tra ScheduleHold tồn tại
            var scheduleHold = _scheduleHoldRepository
                    .findById(scheduleHoldId)
                    .orElseThrow(() -> new RuntimeException("Schedule hold not found: " + scheduleHoldId));
            
            // Kiểm tra bookingId khớp
            if (!scheduleHold.getBookingId().equals(bookingId)) {
                throw new RuntimeException("BookingId mismatch for hold: " + scheduleHoldId);
            }
            
            // Kiểm tra trạng thái là HOLD hoặc BOOKED
            if (!"HOLD".equals(scheduleHold.getStatus()) && !"BOOKED".equals(scheduleHold.getStatus())) {
                throw new RuntimeException("Cannot cancel hold with status: " + scheduleHold.getStatus());
            }
            
            // 2. Nếu status là BOOKED, cần giảm bookedCount
            if ("BOOKED".equals(scheduleHold.getStatus())) {
                var packageSchedule = _healthCheckPackageSchedulesRepository
                        .findById(scheduleHold.getPackageScheduleId())
                        .orElseThrow(() -> new RuntimeException("Package schedule not found: " + scheduleHold.getPackageScheduleId()));
                
                packageSchedule.setBookedCount(packageSchedule.getBookedCount() - 1);
                packageSchedule.setUpdatedAt(ZonedDateTime.now());
                _healthCheckPackageSchedulesRepository.save(packageSchedule);
            }
            
            // 3. Cập nhật ScheduleHold status thành RELEASED
            scheduleHold.setStatus("RELEASED");
            scheduleHold.setUpdatedAt(ZonedDateTime.now());
            _scheduleHoldRepository.save(scheduleHold);
            
            log.info("Hold schedule released: {} for booking: {} (status: {})", scheduleHoldId, bookingId, scheduleHold.getStatus());
            return true;
            
        } catch (Exception e) {
            log.error("Error releasing hold schedule: " + e.getMessage());
            throw e;
        }
    }


    

}
