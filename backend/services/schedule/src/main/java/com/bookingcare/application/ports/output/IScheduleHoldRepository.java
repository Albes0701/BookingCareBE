package com.bookingcare.application.ports.output;

import com.bookingcare.domain.entity.ScheduleHold;
import java.util.Optional;

public interface IScheduleHoldRepository {
    ScheduleHold save(ScheduleHold scheduleHold);
    Optional<ScheduleHold> findById(String id);
    void delete(ScheduleHold scheduleHold);
}