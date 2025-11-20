package com.bookingcare.payment.controller;

import com.bookingcare.payment.dto.common.ApiResponse;
import com.bookingcare.payment.service.interfaceService.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("api/v1/webhooks/callback")
@RequiredArgsConstructor
public class PaymentWebhookController {
    private final PayOS payOS;
    private final PaymentService paymentService;
    @PostMapping()
    public ApiResponse<WebhookData> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            System.out.println(data);
            paymentService.UpdatePaymentStatusByWebhook(data);
            return ApiResponse.success("Webhook delivered", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("webhook signature fail" + e.getMessage());
        }
    }
}
