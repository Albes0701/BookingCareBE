package com.bookingcare.package_service.mapper;

import com.bookingcare.package_service.dto.HealthCheckPackageRequest;
import com.bookingcare.package_service.dto.HealthCheckPackageResponse;
import com.bookingcare.package_service.dto.MedicalServiceRequest;
import com.bookingcare.package_service.dto.MedicalServiceResponse;
import com.bookingcare.package_service.dto.PackageTypeRequest;
import com.bookingcare.package_service.dto.PackageTypeResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceRequest;
import com.bookingcare.package_service.dto.SpecificMedicalServiceResponse;
import com.bookingcare.package_service.entity.HealthCheckPackage;
import com.bookingcare.package_service.entity.MedicalService;
import com.bookingcare.package_service.entity.PackageType;
import com.bookingcare.package_service.entity.SpecificMedicalService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PackageServiceMapper {

    public PackageType toPackageType(PackageTypeRequest request) {
        if (request == null) {
            return null;
        }
        PackageType entity = new PackageType();
        entity.setName(request.name());
        entity.setImage(request.image());
        entity.setSlug(request.slug());
        return entity;
    }

    public void updatePackageTypeFromRequest(PackageTypeRequest request, PackageType entity) {
        if (request == null || entity == null) {
            return;
        }
        if (request.name() != null) {
            entity.setName(request.name());
        }
        entity.setImage(request.image());
        if (request.slug() != null) {
            entity.setSlug(request.slug());
        }
    }

    public PackageTypeResponse toPackageTypeResponse(PackageType entity) {
        if (entity == null) {
            return null;
        }
        return new PackageTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getImage(),
                entity.getSlug(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public MedicalService toMedicalService(MedicalServiceRequest request) {
        if (request == null) {
            return null;
        }
        MedicalService entity = new MedicalService();
        entity.setName(request.name());
        entity.setImage(request.image());
        entity.setSlug(request.slug());
        return entity;
    }

    public void updateMedicalServiceFromRequest(MedicalServiceRequest request, MedicalService entity) {
        if (request == null || entity == null) {
            return;
        }
        if (request.name() != null) {
            entity.setName(request.name());
        }
        entity.setImage(request.image());
        if (request.slug() != null) {
            entity.setSlug(request.slug());
        }
    }

    public MedicalServiceResponse toMedicalServiceResponse(MedicalService entity) {
        if (entity == null) {
            return null;
        }
        return new MedicalServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getImage(),
                entity.getSlug(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public SpecificMedicalService toSpecificMedicalService(SpecificMedicalServiceRequest request) {
        if (request == null) {
            return null;
        }
        SpecificMedicalService entity = new SpecificMedicalService();
        entity.setName(request.name());
        entity.setImage(request.image());
        entity.setSlug(request.slug());
        entity.setDescription(request.description());
        return entity;
    }

    public void updateSpecificMedicalServiceFromRequest(SpecificMedicalServiceRequest request, SpecificMedicalService entity) {
        if (request == null || entity == null) {
            return;
        }
        if (request.name() != null) {
            entity.setName(request.name());
        }
        entity.setImage(request.image());
        if (request.slug() != null) {
            entity.setSlug(request.slug());
        }
        entity.setDescription(request.description());
    }

    public SpecificMedicalServiceResponse toSpecificMedicalServiceResponse(SpecificMedicalService entity) {
        if (entity == null) {
            return null;
        }
        return new SpecificMedicalServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getImage(),
                entity.getSlug(),
                entity.getDescription(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public HealthCheckPackage toHealthCheckPackage(HealthCheckPackageRequest request) {
        if (request == null) {
            return null;
        }
        HealthCheckPackage entity = new HealthCheckPackage();
        applyHealthCheckPackageRequest(request, entity);
        return entity;
    }

    public void updateHealthCheckPackageFromRequest(HealthCheckPackageRequest request, HealthCheckPackage entity) {
        if (request == null || entity == null) {
            return;
        }
        applyHealthCheckPackageRequest(request, entity);
    }

    public void applyHealthCheckPackageRequest(HealthCheckPackageRequest request, HealthCheckPackage entity) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.managedByDoctor() != null) {
            entity.setManagedByDoctor(request.managedByDoctor());
        }
        // keep managingDoctorId untouched – ownership comes from the current user

        if (request.image() != null) {
            entity.setImage(request.image());
        }
        if (request.packageDetailInfo() != null) {
            entity.setPackageDetailInfo(request.packageDetailInfo());
        }
        if (request.shortPackageInfo() != null) {
            entity.setShortPackageInfo(request.shortPackageInfo());
        }
        if (request.slug() != null) {
            entity.setSlug(request.slug());
        }
        
        if (request.submittedAt() != null) {
            entity.setSubmittedAt(request.submittedAt());
        }
        if (request.approvedAt() != null) {
            entity.setApprovedAt(request.approvedAt());
        }
        
        if (request.packageTypeId() != null) {
            PackageType packageType = entity.getPackageType();
            if (packageType == null) {
                packageType = new PackageType();
                entity.setPackageType(packageType);
            }
            packageType.setId(request.packageTypeId());
        }
    }



    public HealthCheckPackageResponse toHealthCheckPackageResponse(HealthCheckPackage entity) {
        if (entity == null) {
            return null;
        }
        UUID packageTypeId = entity.getPackageType() != null ? entity.getPackageType().getId() : null;
        return new HealthCheckPackageResponse(
                entity.getId(),
                entity.getName(),
                entity.isManagedByDoctor(),
                entity.getManagingDoctorId(),
                entity.getImage(),
                packageTypeId,
                entity.getPackageDetailInfo(),
                entity.getShortPackageInfo(),
                entity.getSlug(),
                entity.getStatus(),
                entity.getRejectedReason(),
                entity.getSubmittedAt(),
                entity.getApprovedAt(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
