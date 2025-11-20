package com.bookingcare.application.ports.output;

import com.bookingcare.application.dto.PackageDetailResponse;

public interface IHealthPackageServicePort {
    PackageDetailResponse getPackageDetailBySlug(String slug);
}