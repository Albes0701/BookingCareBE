package com.bookingcare.payment.controller;

import com.bookingcare.payment.dto.CreatePaymentLinkRequestBody;
import com.bookingcare.payment.dto.PaymentRequestCreate;
import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.dto.PaymentStatusUpdateRequest;
import com.bookingcare.payment.dto.common.ApiResponse;
import com.bookingcare.payment.service.interfaceService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(@RequestBody PaymentRequestCreate request) {
        // Chỉ cần gọi hàm từ interface
        PaymentResponseDTO response = paymentService.CreatePaymentAsync(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        // 1. Gọi service để lấy danh sách
        List<PaymentResponseDTO> responseList = paymentService.GetAllPaymentsAsync();

        // 2. Trả về 200 OK cùng với danh sách
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable String id) {
        try {
            // 1. Gọi service để lấy
            PaymentResponseDTO response = paymentService.GetPaymentByIdAsync(id);

            // 2. Trả về 200 OK
            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            // 3. Nếu Service ném Exception (do .orElseThrow), bắt lại
            //    và trả về 404 NOT FOUND.
            log.warn("Could not find payment with id {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<PaymentResponseDTO> getPaymentByBookingId(@RequestParam("bookingId") String bookingId) {

        log.info("Received search request for bookingId: {}", bookingId);

        try {
            // 1. Gọi service
            PaymentResponseDTO response = paymentService.GetPaymentByBookingIdAsync(bookingId);

            // 2. Trả về 200 OK
            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            // 3. Bắt lỗi (nếu service ném ra do .orElseThrow())
            //    và trả về 404 NOT FOUND.
            log.warn("Could not find payment for bookingId {}: {}", bookingId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable String id,
            @RequestBody PaymentStatusUpdateRequest request) {

        try {
            PaymentResponseDTO response = paymentService.UpdatePaymentAsync(id, request);
            return ResponseEntity.ok(response); // Trả về 200 OK

        } catch (RuntimeException ex) {
            // Bắt lỗi từ .orElseThrow() (Không tìm thấy ID)
            log.warn("Failed to update status. Payment not found with id {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Trả về 404

        }
    }

    @PostMapping(path = "/create")
    public ResponseEntity<ApiResponse<CreatePaymentLinkResponse>> createPaymentLink(
            @RequestBody CreatePaymentLinkRequestBody requestBody) {

        try {
            // Logic tạo link (từ service)
            CreatePaymentLinkResponse data = paymentService.createPaymentLink(requestBody);
            return ResponseEntity.ok(ApiResponse.success(data));

        } catch (Exception e) {
            log.error("Failed to create payment link: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create payment link: " + e.getMessage()));
        }
    }
}
