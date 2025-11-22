package com.bookingcare.application.ports.input;

import java.time.LocalDate;
import java.util.List;

import com.bookingcare.application.dto.QueryHealthCheckPackageSchedulesResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;

public interface IScheduleApplicationServicePatient {
    // Tìm schedule dựa vào packageID
    List<QueryPackageScheduleResponse> getPackageScheduleByPackageId(String packageId);

    // Tìm schedule dựa vào healthCheckPackageSlug và date
    List<QueryPackageScheduleResponse> getHealthCheckPackageSchedulesBySlugAndDate(String healthCheckPackageSlug, LocalDate date);
    // Boolean updateHealthCheckPackageSchedules(UpdateHealthCheckPackageSchedulesCommand command);

    // Lấy chi tiết thông tin của HealthCheckPackageSchedule bằng ID
    QueryHealthCheckPackageSchedulesResponse getHealthCheckPackageScheduleById(String packageScheduleId);

    // Lấy schedule dựa vào doctorId
    List<QueryPackageScheduleResponse> getPackageScheduleByDoctorId(String doctorId);

    // Internal - Giữ chỗ cho booking
    String holdScheduleForBooking(String packageScheduleId, String bookingId);

    // Internal - Xác nhận giữ chỗ cho booking
    Boolean confirmHoldScheduleForBooking(String scheduleHoldId, String bookingId);

    // Internal - Hết hạn giữ chỗ cho booking
    Boolean expiredHoldScheduleForBooking(String scheduleHoldId, String bookingId);


    // Internal - Hủy giữ chỗ cho booking
    Boolean cancelHoldScheduleForBooking(String scheduleHoldId, String bookingId);

    // Internal - Kiểm tra lịch còn trống không
    Boolean isScheduleAvailable(String packageScheduleId);


}
