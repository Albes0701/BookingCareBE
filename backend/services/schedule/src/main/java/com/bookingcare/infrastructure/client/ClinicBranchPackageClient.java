package com.bookingcare.infrastructure.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookingcare.infrastructure.client.dto.ClinicBranchPackageResponse;

@FeignClient(
        name = "clinic-service",
        url = "${application.config.clinic-service-url}",
        configuration = FeignConfig.class
)
public interface ClinicBranchPackageClient {

    @GetMapping("/branches/package/{clinicBranchId}")
    List<ClinicBranchPackageResponse> getClinicBranchesByPackageId(
            @PathVariable("clinicBranchId") String clinicBranchId
    );
    
}
