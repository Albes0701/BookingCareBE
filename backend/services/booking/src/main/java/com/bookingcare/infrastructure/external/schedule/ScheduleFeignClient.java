package com.bookingcare.infrastructure.external.schedule;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.ports.output.IScheduleFeignClientService;

import java.util.Optional;

@FeignClient(name = "schedule-service", url = "${application.config.schedule-url}")
public interface ScheduleFeignClient extends IScheduleFeignClientService {
    @GetMapping("/{packageScheduleId}")
    ApiResponse<Optional<QueryPackageScheduleResponse>> getPackageScheduleById(
            @PathVariable String packageScheduleId);

}
