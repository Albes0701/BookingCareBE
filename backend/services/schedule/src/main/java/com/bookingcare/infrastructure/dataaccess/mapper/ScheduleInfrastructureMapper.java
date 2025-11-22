package com.bookingcare.infrastructure.dataaccess.mapper;

import org.springframework.stereotype.Component;

import com.bookingcare.domain.entity.HealthCheckPackageSchedule;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleDoctor;
import com.bookingcare.domain.entity.Schedule;
import com.bookingcare.domain.entity.ScheduleHold;
import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleJpaEntity;
import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleDoctorJpaEntity;
import com.bookingcare.infrastructure.dataaccess.entity.ScheduleJpaEntity;
import com.bookingcare.infrastructure.dataaccess.entity.ScheduleHoldJpaEntity;

@Component
public class ScheduleInfrastructureMapper {
    
    // HealthCheckPackageSchedule Mappings
    public HealthCheckPackageSchedule toDomain(HealthCheckPackageScheduleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return HealthCheckPackageSchedule.builder()
                .packageScheduleId(entity.getPackageScheduleId())
                .packageId(entity.getPackageId())
                .scheduleId(entity.getScheduleId())
                .scheduleDate(entity.getScheduleDate())
                .capacity(entity.getCapacity())
                .bookedCount(entity.getBookedCount())
                .overbookLimit(entity.getOverbookLimit())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public HealthCheckPackageScheduleJpaEntity toJpaEntity(HealthCheckPackageSchedule domain) {
        if (domain == null) {
            return null;
        }

        return HealthCheckPackageScheduleJpaEntity.builder()
                .packageScheduleId(domain.getPackageScheduleId())
                .packageId(domain.getPackageId())
                .scheduleId(domain.getScheduleId())
                .scheduleDate(domain.getScheduleDate())
                .capacity(domain.getCapacity())
                .bookedCount(domain.getBookedCount())
                .overbookLimit(domain.getOverbookLimit())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }

    // HealthCheckPackageScheduleDoctor Mappings
    public HealthCheckPackageScheduleDoctor toDomain(HealthCheckPackageScheduleDoctorJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return HealthCheckPackageScheduleDoctor.builder()
                .id(entity.getId())
                .packageScheduleId(entity.getPackageScheduleId())
                .doctorId(entity.getDoctorId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public HealthCheckPackageScheduleDoctorJpaEntity toJpaEntity(HealthCheckPackageScheduleDoctor domain) {
        if (domain == null) {
            return null;
        }

        return HealthCheckPackageScheduleDoctorJpaEntity.builder()
                .id(domain.getId())
                .packageScheduleId(domain.getPackageScheduleId())
                .doctorId(domain.getDoctorId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }

    // Schedule Mappings
    public Schedule toDomain(ScheduleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Schedule.builder()
                .id(entity.getId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .day(entity.getDayId())
                .build();
    }

    public ScheduleJpaEntity toJpaEntity(Schedule domain) {
        if (domain == null) {
            return null;
        }

        return ScheduleJpaEntity.builder()
                .id(domain.getId())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .dayId(domain.getDay())
                .build();
    }

    // ScheduleHold Mappings
    public ScheduleHold toDomain(ScheduleHoldJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ScheduleHold.builder()
                .id(entity.getId())
                .packageScheduleId(entity.getPackageScheduleId())
                .bookingId(entity.getBookingId())
                .status(entity.getStatus())
                .expireAt(entity.getExpireAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ScheduleHoldJpaEntity toJpaEntity(ScheduleHold domain) {
        if (domain == null) {
            return null;
        }

        return ScheduleHoldJpaEntity.builder()
                .id(domain.getId())
                .packageScheduleId(domain.getPackageScheduleId())
                .bookingId(domain.getBookingId())
                .status(domain.getStatus())
                .expireAt(domain.getExpireAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
