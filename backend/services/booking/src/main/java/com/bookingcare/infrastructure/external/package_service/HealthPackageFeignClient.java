package com.bookingcare.infrastructure.external.package_service;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookingcare.infrastructure.external.FeignConfig;

@FeignClient(
            name = "package-service", 
            url = "${application.config.package-url}",
            configuration = FeignConfig.class
        )
public interface HealthPackageFeignClient {
    @GetMapping("/packages/{id}")
    ResponseEntity<HealthCheckPackageResponse> getPackageDetail(@PathVariable UUID id);
}
        
