package com.bookingcare.package_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookingcare.package_service.dto.DoctorsResponseDTO;





@FeignClient(
        name = "expertise-service",
        url = "${application.config.expertise-url}",
        configuration = FeignConfig.class
)
public interface ExpertiseClient {
    @GetMapping("/doctors/by-user/{userId}")
    DoctorsResponseDTO getDoctorByUserId(@PathVariable("userId") String userId);

}

