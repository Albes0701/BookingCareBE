package com.bookingcare.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payos") // 1. Tìm prefix "payos" trong YAML
@Data // 2. Tự tạo Getter/Setter
public class PayOSConfig {

    // 3. Tự động "bơm" các giá trị vào biến
    private String clientId;
    private String apiKey;
    private String checksumKey;

    // Lưu ý: Spring Boot tự động map "client-id" (YAML)
    //        thành "clientId" (Java camelCase)
}
