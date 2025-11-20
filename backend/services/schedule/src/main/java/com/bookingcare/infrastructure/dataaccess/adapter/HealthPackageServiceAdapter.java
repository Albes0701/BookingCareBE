package com.bookingcare.infrastructure.dataaccess.adapter;

import org.springframework.stereotype.Component;
import com.bookingcare.application.dto.PackageDetailResponse;
import com.bookingcare.application.ports.output.IHealthPackageServicePort;
import com.bookingcare.infrastructure.client.HealthPackageServiceClient;
import com.bookingcare.infrastructure.dataaccess.mapper.PackageServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthPackageServiceAdapter implements IHealthPackageServicePort {
    private final HealthPackageServiceClient healthPackageServiceClient;
    private final PackageServiceMapper packageServiceMapper;

    @Override
    public PackageDetailResponse getPackageDetailBySlug(String slug) {
        try {
            var response = healthPackageServiceClient.getPackageDetailBySlug(slug);
            return packageServiceMapper.mapToPackageDetail(response.getBody());
        } catch (Exception e) {
            log.error("Error calling package service: " + e.getMessage());
            throw new RuntimeException("Failed to fetch package details", e);
        }
    }
}