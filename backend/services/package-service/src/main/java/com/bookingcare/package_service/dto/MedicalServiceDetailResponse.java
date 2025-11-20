package com.bookingcare.package_service.dto;

import java.util.List;

public record MedicalServiceDetailResponse(
        MedicalServiceResponse service,
        List<SpecificMedicalServiceResponse> specificMedicalServices
) {
}
