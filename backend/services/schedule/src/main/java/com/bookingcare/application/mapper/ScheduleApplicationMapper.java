package com.bookingcare.application.mapper;

import org.springframework.stereotype.Component;

import com.bookingcare.application.dto.QueryHealthCheckPackageSchedulesResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.dto.QueryScheduleResponse;
import com.bookingcare.application.dto.QueryScheduleHoldResponse;
import com.bookingcare.domain.entity.HealthCheckPackageSchedule;
import com.bookingcare.domain.entity.Schedule;
import com.bookingcare.domain.entity.ScheduleHold;

@Component
public class ScheduleApplicationMapper {

    public QueryPackageScheduleResponse toQueryPackageScheduleResponse(HealthCheckPackageSchedule healthCheckPackageSchedule, Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        return new QueryPackageScheduleResponse(
                healthCheckPackageSchedule.getPackageScheduleId(),
                healthCheckPackageSchedule.getPackageId(),
                toQueryScheduleResponse(schedule),
                healthCheckPackageSchedule.getScheduleDate(),
                healthCheckPackageSchedule.getIsDeleted());
    }

    public QueryScheduleResponse toQueryScheduleResponse(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        return new QueryScheduleResponse(
                schedule.getId(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getDay().toString());
    }

    public QueryHealthCheckPackageSchedulesResponse toQueryHealthCheckPackageSchedulesResponse(
            HealthCheckPackageSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        return new QueryHealthCheckPackageSchedulesResponse(
                schedule.getPackageScheduleId(),
                schedule.getPackageId(),
                schedule.getScheduleId(),
                schedule.getScheduleDate(),
                schedule.getCapacity(),
                schedule.getBookedCount(),
                schedule.getOverbookLimit(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt(),
                schedule.getIsDeleted());
    }

    public QueryScheduleHoldResponse toQueryScheduleHoldResponse(ScheduleHold hold) {
        if (hold == null) {
            return null;
        }

        return new QueryScheduleHoldResponse(
                hold.getId(),
                hold.getPackageScheduleId(),
                hold.getBookingId(),
                hold.getStatus(),
                hold.getExpireAt(),
                hold.getCreatedAt(),
                hold.getUpdatedAt());
    }
}
