package com.bookingcare.application.ports.output;

import java.util.Optional;

import com.bookingcare.domain.entity.Schedule;

public interface IScheduleRepository {
    Optional<Schedule> findById(String packageScheduleId);
}
