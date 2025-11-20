package com.bookingcare.container.config;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

@Configuration
public class AuditingConfig {

    private static final ZoneId UTC_PLUS_7 = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * This bean tells Spring JPA Auditing to use ZonedDateTime.now()
     * with the "Asia/Ho_Chi_Minh" (GMT+7) timezone
     * when populating @CreatedDate and @LastModifiedDate fields.
     */
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now(UTC_PLUS_7));
    }
}
