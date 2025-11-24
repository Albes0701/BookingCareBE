package com.bookingcare.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSlotSucceededEvent {
    private String bookingId;
    private String scheduleHoldId;
    private ZonedDateTime holdExpireAt;
    private String packageScheduleId;
}