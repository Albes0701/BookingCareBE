package com.bookingcare.application.mapper;

import org.springframework.stereotype.Component;

import com.bookingcare.application.dto.CreateBookingCommand;
import com.bookingcare.application.dto.CreateBookingResponse;
import com.bookingcare.application.dto.QueryBookingOrderDetailInfoResponse;
import com.bookingcare.application.dto.QueryOrdersResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.dto.QueryScheduleResponse;
import com.bookingcare.domain.entity.BookingPackageDetail;
import com.bookingcare.domain.entity.HealthCheckPackageSchedule;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;
import com.bookingcare.domain.entity.Schedule;
import com.bookingcare.domain.valueobject.DayId;
import com.bookingcare.domain.valueobject.PurchaseMethod;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingMapperApplication {
    private Boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public HealthCheckPackageScheduleBookingDetail toEntity(CreateBookingCommand command) {
        // Map fields from command to entity
        return HealthCheckPackageScheduleBookingDetail.builder()
                .patientRelativesName(isNullOrEmpty(command.patientRelativeName()) ? "" : command.patientRelativeName())
                .patientRelativesPhoneNumber(
                        isNullOrEmpty(command.patientRelativePhone()) ? "" : command.patientRelativePhone())
                .patientId(isNullOrEmpty(command.patientId()) ? "" : command.patientId())
                .packageScheduleId(isNullOrEmpty(command.packageScheduleId()) ? "" : command.packageScheduleId())
                .bookingPackageId(isNullOrEmpty(command.bookingPackageId()) ? "" : command.bookingPackageId())
                .bookingReason(isNullOrEmpty(command.bookingReason()) ? "" : command.bookingReason())
                .clinicId(isNullOrEmpty(command.clinicBranchId()) ? "" : command.clinicBranchId())
                .purchaseMethod(isNullOrEmpty(command.purchaseMethod()) ? null
                        : PurchaseMethod.valueOf(command.purchaseMethod()))
                .build();
    }

    public CreateBookingResponse toCommand(HealthCheckPackageScheduleBookingDetail entity) {
        return new CreateBookingResponse(entity.getId());
    }

    public HealthCheckPackageSchedule toEntity(QueryPackageScheduleResponse dto) {
        if (dto == null) {
            return null;
        }

        return HealthCheckPackageSchedule.builder()
                .packageScheduleId(dto.packageScheduleId())
                .packageId(dto.packageId())
                .scheduleDate(dto.scheduleDate())
                .schedule(toEntity(dto.schedule()))
                .isDeleted(dto.isDeleted())
                .build();
    }

    public Schedule toEntity(QueryScheduleResponse dto) {
        if (dto == null) {
            return null;
        }

        return Schedule.builder()
                .id(dto.id())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .day(DayId.fromString(dto.dayId()))
                .build();
    }

    public QueryOrdersResponse toQueryOrdersResponse(HealthCheckPackageScheduleBookingDetail booking) {
        if (booking == null) {
            return null;
        }

        return new QueryOrdersResponse(
                booking.getId(),
                "Trần Văn B",
                "Gói khám sức khỏe tổng quát chuyên sâu dành cho nữ (PKYD4F)",
                "Bệnh viện nhân dân Gia Định",
                booking.getBookingStatus().toString(),
                booking.getCreatedDate(),
                booking.getUpdatedDate());
    }

    public QueryBookingOrderDetailInfoResponse toQueryBookingOrderDetailInfoResponse(
        HealthCheckPackageScheduleBookingDetail booking, 
        QueryPackageScheduleResponse packageSchedule,
        BookingPackageDetail packageDetail
        ) {
        if (booking == null) {
            return null;
        }

        return new QueryBookingOrderDetailInfoResponse(
                booking.getId(),
                booking.getPatientRelativesName(),
                booking.getPatientRelativesPhoneNumber(),
                booking.getBookingReason(),
                booking.getBookingStatus().toString(),
                booking.getPurchaseMethod().toString(),
                                new QueryBookingOrderDetailInfoResponse.PatientInfo(
                        "PA001",
                        "Nguyễn Văn B",
                        "0123456789",
                        "example@gmail.com",
                        "273 An Dương Vương, Phường 3, Quận 5, TP.HCM"
                ),
                new QueryBookingOrderDetailInfoResponse.ScheduleInfo(
                        packageSchedule.packageScheduleId(),
                        "HCKPG046",
                        "Gói khám sức khỏe tổng quát chuyên sâu dành cho nữ (PKYD4F)",
                        packageSchedule.schedule().startTime() + " - " + packageSchedule.schedule().endTime(),
                        packageSchedule.scheduleDate(),
                        new QueryBookingOrderDetailInfoResponse.ScheduleInfo.BookingPackageInfo(
                                packageDetail.getBookingPackage().getName(),
                                packageDetail.getPrice()
                        )
                ),
                new QueryBookingOrderDetailInfoResponse.ClinicInfo(
                        "Phòng khám Bệnh viện Đại học Y Dược 1",
                        "Cơ sở chính",
                        "20-22 Đường Quang Trung, Phường 12, Quận 10, Tp. HCM"
                ),
                booking.getCreatedDate(),
                booking.getUpdatedDate()
        );
    }
}
