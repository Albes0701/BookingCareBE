package com.bookingcare.container.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.CreateBookingCommand;
import com.bookingcare.application.dto.CreateBookingResponse;
import com.bookingcare.application.dto.HealthCheckBookHistoryResponse;
import com.bookingcare.application.dto.QueryBookingOrderDetailInfoResponse;
import com.bookingcare.application.dto.QueryBookingPackageDetailInfo;
import com.bookingcare.application.dto.QueryOrdersResponse;
import com.bookingcare.application.dto.UpdateBookingOrderStatusCommand;
import com.bookingcare.application.ports.input.IBookingApplicationService;
import com.bookingcare.infrastructure.external.package_service.HealthCheckPackageResponse;
import com.bookingcare.infrastructure.external.package_service.HealthPackageFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/booking")
@Slf4j
public class BookingController {
    
    private final IBookingApplicationService _bookingApplicationService;
    private final HealthPackageFeignClient _healthPackageFeignClient;  // Thêm dòng này



    @GetMapping(value = "test")
    public ResponseEntity<Object> test() {
        return ResponseEntity.ok(Map.of("message", "Test with hot reloading", "status", 200));
    }

    @GetMapping("get-all-orders")
    public ApiResponse<List<QueryOrdersResponse>> getAllBookingOrders() {
        log.info("Fetching all booking orders");

        List<QueryOrdersResponse> bookings = _bookingApplicationService.getAllBookingOrders();
        String message = bookings.isEmpty() ? "No bookings found" : "Bookings fetched successfully";
        return new ApiResponse<>(200, message, bookings);
    }

    @GetMapping("{id}")
    public ApiResponse<QueryBookingOrderDetailInfoResponse> getBookingsOrderDetailInfo(@PathVariable String id) {
        log.info("Fetching bookings for booking id: {}", id);

        QueryBookingOrderDetailInfoResponse booking = _bookingApplicationService.getBookingsOrderDetailInfo(id);
        String message = booking == null ? "No bookings found" : "Bookings fetched successfully";
        return new ApiResponse<>(200, message, booking);
    }

    @PostMapping("submit-booking")
    public ApiResponse<CreateBookingResponse> createBookingOrder(@RequestBody CreateBookingCommand command) {
        log.info("Received booking command: {}", command);

        String orderId = _bookingApplicationService.createBooking(command);
        CreateBookingResponse response = new CreateBookingResponse(orderId);
        return new ApiResponse<>(200, "Booking created successfully", response);
    }

    @GetMapping("patient/{id}")
    public ApiResponse<List<QueryOrdersResponse>> getBookingsByPatientId(@PathVariable String id) {
        log.info("Fetching bookings for patient id: {}", id);

        List<QueryOrdersResponse> bookings = _bookingApplicationService.getBookingByPatientId(id);
        String message = bookings.isEmpty() ? "No bookings found for the patient" : "Bookings fetched successfully";
        return new ApiResponse<>(200, message, bookings);
    }
    
    @GetMapping("clinic/{id}")
    public ApiResponse<List<QueryOrdersResponse>> getBookingByClinicId(@PathVariable String id) {
        log.info("Fetching bookings for clinic id: {}", id);

        List<QueryOrdersResponse> bookings = _bookingApplicationService.getBookingByClinicId(id);
        String message = bookings.isEmpty() ? "No bookings found for the clinic" : "Bookings fetched successfully";
        return new ApiResponse<>(200, message, bookings);
    }

    @PatchMapping("update-booking-status")
    public ApiResponse<String> updateBookingStatus(@RequestBody UpdateBookingOrderStatusCommand cmd) {
        log.info("Updating booking status for id: {}", cmd.orderId());

        boolean isUpdated = _bookingApplicationService.updateBookingStatus(cmd.orderId(), cmd.status());
        String message = isUpdated ? "Booking status updated successfully" : "Failed to update booking status";
        return new ApiResponse<>(200, message, null);
    }

   /**
     * ✅ Get payment URL for QR code
     * Frontend polls this endpoint after creating booking
    */
    @GetMapping("/{bookingId}/payment-url")
    public ApiResponse<Map<String, Object>> getPaymentUrl(@PathVariable String bookingId) {
        log.info("Fetching payment URL for booking: {}", bookingId);
        
        Map<String, Object> result = _bookingApplicationService.getPaymentUrl(bookingId);
        
        String status = (String) result.get("status");
        int httpStatus = "ERROR".equals(status) ? 500 : 200;
        String message = (String) result.get("message");
        
        return new ApiResponse<>(httpStatus, message, result);
    }

    @GetMapping("/booking-package-details")
    public ApiResponse<List<QueryBookingPackageDetailInfo>> getAllBookingPackageDetails() {
        log.info("Fetching all booking package details");

        List<QueryBookingPackageDetailInfo> bookingDetails = _bookingApplicationService.getAllBookingPackageDetails();
        String message = bookingDetails.isEmpty() ? "No booking package details found" : "Booking package details fetched successfully";
        return new ApiResponse<>(200, message, bookingDetails);
    }


    @GetMapping("/test-feign/{packageId}")
    public ApiResponse<HealthCheckPackageResponse> testFeignClient(@PathVariable String packageId) {
        log.info("Testing FeignClient connection with packageId: {}", packageId);
        
        try {
            // Validate packageId format
            java.util.UUID packageUUID = java.util.UUID.fromString(packageId);
            log.info("Converted packageId to UUID: {}", packageUUID);
            
            // Call FeignClient
            log.info("Calling FeignClient.getPackageDetail()...");
            ResponseEntity<HealthCheckPackageResponse> responseEntity = _healthPackageFeignClient
                    .getPackageDetail(packageUUID);
            
            log.info("FeignClient response status: {}", responseEntity.getStatusCode());
            
            
            HealthCheckPackageResponse packageResponse = responseEntity.getBody();
            
            if (packageResponse == null) {
                log.warn("FeignClient response body is null");
                return new ApiResponse<>(404, "Package not found", null);
            }
            
            log.info("Successfully fetched package: {}", packageResponse.name());
            return new ApiResponse<>(200, "Package fetched successfully from package-service", packageResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for packageId: {}", packageId);
            return new ApiResponse<>(400, "Invalid package ID format", null);
        } catch (Exception e) {
            log.error("Error calling FeignClient: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error calling package-service: " + e.getMessage(), null);
        }
    }


    @GetMapping("/book-history/{id}")
    public ApiResponse<List<HealthCheckBookHistoryResponse>> getBookingHistoryByPatientId(@PathVariable String id) {
        log.info("Fetching booking history for patient id: {}", id);

        List<HealthCheckBookHistoryResponse> bookings = _bookingApplicationService.getBookingHistoryByPatientId(id);
        String message = bookings.isEmpty() ? "No booking history found for the patient" : "Booking history fetched successfully";
        return new ApiResponse<>(200, message, bookings);
    }

    




}
