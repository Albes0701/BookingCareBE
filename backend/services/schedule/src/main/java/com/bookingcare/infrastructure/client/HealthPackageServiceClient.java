package com.bookingcare.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookingcare.infrastructure.client.dto.HealthCheckPackageResponse;






@FeignClient(
        name = "package-service",
        url = "${application.config.package-service-url}",
        configuration = FeignConfig.class
)
public interface HealthPackageServiceClient {
    

    @GetMapping("/packages/slug/{slug}")
    ResponseEntity<HealthCheckPackageResponse> getPackageDetailBySlug(@PathVariable("slug") String slug);

}

