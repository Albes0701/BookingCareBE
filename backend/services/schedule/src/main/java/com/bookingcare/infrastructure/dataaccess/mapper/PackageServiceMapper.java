package com.bookingcare.infrastructure.dataaccess.mapper;



import org.springframework.stereotype.Component;
import com.bookingcare.application.dto.PackageDetailResponse;
import com.bookingcare.infrastructure.client.dto.HealthCheckPackageResponse;

@Component
public class PackageServiceMapper {

    // Map từ package-service DTO sang schedule-service DTO
    public PackageDetailResponse mapToPackageDetail(HealthCheckPackageResponse packageResponse) {
        if (packageResponse == null) {
            return null;
        }
        
        return new PackageDetailResponse(
                packageResponse.id(),
                packageResponse.name(),
                packageResponse.slug(),
                packageResponse.image(),
                packageResponse.deleted(),
                packageResponse.createdAt(),
                packageResponse.updatedAt()
        );
    }
}