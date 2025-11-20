package com.bookingcare.application.ports.output;

import java.util.Optional;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;

public interface IScheduleFeignClientService {
    ApiResponse<Optional<QueryPackageScheduleResponse>> getPackageScheduleById(String packageScheduleId);
}
