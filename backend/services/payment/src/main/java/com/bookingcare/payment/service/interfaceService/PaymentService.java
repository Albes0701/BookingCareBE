package com.bookingcare.payment.service.interfaceService;

import java.util.List;


import com.bookingcare.payment.dto.CreatePaymentLinkRequestBody;
import com.bookingcare.payment.dto.PaymentRequestCreate;
import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.dto.PaymentStatusUpdateRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;


public interface PaymentService {
    List<PaymentResponseDTO> GetAllPaymentsAsync();
    PaymentResponseDTO GetPaymentByIdAsync(String paymentId);
    PaymentResponseDTO CreatePaymentAsync(PaymentRequestCreate paymentRequestCreate);
    PaymentResponseDTO UpdatePaymentAsync(String id, PaymentStatusUpdateRequest request);
    PaymentResponseDTO GetPaymentByBookingIdAsync(String bookingId);
    // thanh toán online
    CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequestBody requestBody) throws Exception;
    void UpdatePaymentStatusByWebhook(WebhookData data);

    // ...existing methods...

    /**
     * Get payment by order code (từ webhook)
     */
    PaymentResponseDTO GetPaymentByOrderCodeAsync(long orderCode);


}
