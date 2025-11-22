package com.bookingcare.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicBranchPackageResponse {
    String id;
    String clinicBranchId;
    String healthcheckPackageId;
    boolean isDeleted;
}