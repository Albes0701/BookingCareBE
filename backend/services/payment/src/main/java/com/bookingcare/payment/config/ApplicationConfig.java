package com.bookingcare.payment.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final PayOSConfig payOSConfig; // Tiêm config từ Bước 2

    @Bean
    public PayOS payOS() {
        // Sử dụng builder để khởi tạo
        return new PayOS(ClientOptions.builder()
                .clientId(payOSConfig.getClientId())
                .apiKey(payOSConfig.getApiKey())
                .checksumKey(payOSConfig.getChecksumKey())
                .build());
    }
}
