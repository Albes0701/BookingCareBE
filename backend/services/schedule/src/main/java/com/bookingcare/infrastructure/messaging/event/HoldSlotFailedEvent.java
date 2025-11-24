package com.bookingcare.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// HoldSlotFailedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSlotFailedEvent {
    private String bookingId;
    private String reason;
}
