package com.bookingcare.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    private String eventId;
    private String eventType;
    private String aggregateId;      // bookingId
    private String correlationId;    // Trace toàn bộ saga
    private ZonedDateTime timestamp;
    private String source;           // booking-service, schedule-service...
    private T payload;

    public static <T> EventEnvelope<T> of(String eventType, String aggregateId, 
                                           String correlationId, String source, T payload) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .aggregateId(aggregateId)
                .correlationId(correlationId)
                .timestamp(ZonedDateTime.now())
                .source(source)
                .payload(payload)
                .build();
    }
}