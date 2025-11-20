package com.bookingcare.infrastructure.dataaccess.mapper;

import org.springframework.stereotype.Component;

import com.bookingcare.domain.entity.BookingPackage;
import com.bookingcare.domain.entity.BookingPackageDetail;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;
import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageDetailJpaEntity;
import com.bookingcare.infrastructure.dataaccess.entity.BookingPackageJpaEntity;
import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleBookingDetailJpaEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingMapperInfrastructure {
    public BookingPackage toDomain(BookingPackageJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return BookingPackage.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public BookingPackageJpaEntity toEntity(BookingPackage domain) {
        return BookingPackageJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    public HealthCheckPackageScheduleBookingDetailJpaEntity toJpaEntity(
            HealthCheckPackageScheduleBookingDetail domain) {
        // Mapping logic here
        return HealthCheckPackageScheduleBookingDetailJpaEntity.builder()
                .id(domain.getId())
                .patientRelativesName(domain.getPatientRelativesName())
                .patientRelativesPhoneNumber(domain.getPatientRelativesPhoneNumber())
                .patientId(domain.getPatientId())
                .packageScheduleId(domain.getPackageScheduleId())
                .bookingPackageId(domain.getBookingPackageId())
                .bookingReason(domain.getBookingReason())
                .clinicId(domain.getClinicId())
                .bookingStatus(domain.getBookingStatus())
                .purchaseMethod(domain.getPurchaseMethod())
                .build();
    }

    public HealthCheckPackageScheduleBookingDetail toDomain(
            HealthCheckPackageScheduleBookingDetailJpaEntity entity) {

        if (entity == null) {
            return null;
        }

        return HealthCheckPackageScheduleBookingDetail.builder()
                .id(entity.getId())
                .patientRelativesName(entity.getPatientRelativesName())
                .patientRelativesPhoneNumber(entity.getPatientRelativesPhoneNumber())
                .patientId(entity.getPatientId())
                .packageScheduleId(entity.getPackageScheduleId())
                .bookingPackageId(entity.getBookingPackageId())
                .bookingReason(entity.getBookingReason())
                .clinicId(entity.getClinicId())
                .bookingStatus(entity.getBookingStatus())
                .purchaseMethod(entity.getPurchaseMethod())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .bookingPackage(toDomain(entity.getBookingPackage()))
                .build();
    }

    public BookingPackageDetail toDomain(BookingPackageDetailJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return BookingPackageDetail.builder()
                .bookingPackageId(entity.getId().getBookingPackageId())
                .packageId(entity.getId().getPackageId())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .bookingPackage(toDomain(entity.getBookingPackage()))
                .build();
    }
}
