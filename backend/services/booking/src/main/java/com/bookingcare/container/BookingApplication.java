package com.bookingcare.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.bookingcare.container",
		"com.bookingcare.domain",
		"com.bookingcare.infrastructure",
		"com.bookingcare.application"
})
@EnableJpaRepositories(basePackages = "com.bookingcare.infrastructure.dataaccess.repository")
@EntityScan(basePackages = "com.bookingcare.infrastructure.dataaccess.entity")
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableFeignClients(basePackages = {
		"com.bookingcare.infrastructure.external"
})
public class BookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingApplication.class, args);
	}

}
