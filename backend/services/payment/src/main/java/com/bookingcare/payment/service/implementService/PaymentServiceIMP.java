package com.bookingcare.payment.service.implementService;

import com.bookingcare.payment.dto.CreatePaymentLinkRequestBody;
import com.bookingcare.payment.dto.PaymentRequestCreate;
import com.bookingcare.payment.dto.PaymentResponseDTO;
import com.bookingcare.payment.dto.PaymentStatusUpdateRequest;
import com.bookingcare.payment.entity.Payment;
import com.bookingcare.payment.entity.Status;
import com.bookingcare.payment.mapper.PaymentMapper;
import com.bookingcare.payment.repository.PaymentRepository;
import com.bookingcare.payment.service.interfaceService.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class PaymentServiceIMP implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PayOS payOS;
    @Override
    public PaymentResponseDTO CreatePaymentAsync(PaymentRequestCreate requestDTO){
        log.info("Creating payment with order code: {}", requestDTO);
        // 1. Chuyển đổi DTO (input) sang Entity
        Payment newPayment = PaymentMapper.toPayment(requestDTO);
        log.info("Converted PaymentRequestCreate to Payment entity: {}", newPayment);
        // 2. Lưu Entity vào database
        Payment savedPayment = paymentRepository.save(newPayment);
        // 3. Chuyển đổi Entity (đã lưu) sang DTO (output)
        return PaymentMapper.toPaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> GetAllPaymentsAsync(){
        log.info("Fetching all payments...");

        // 1. Gọi repository để lấy TẤT CẢ entity Payment từ DB
        List<Payment> payments = paymentRepository.findAll();

        // 2. Dùng Java Stream để chuyển (map) danh sách List<Payment>
        //    thành danh sách List<PaymentResponseDTO>
        List<PaymentResponseDTO> responseDTOs = payments.stream()
                .map(PaymentMapper::toPaymentResponse) // Dùng hàm static
                // .map(PaymentMapper::toPaymentResponse) // Cách viết ngắn gọn hơn
                .collect(Collectors.toList());

        log.info("Found {} payments.", responseDTOs.size());
        return responseDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO GetPaymentByIdAsync(String id) {
        log.info("Fetching payment with id: {}", id);

        // 1. Tìm payment bằng ID trong DB.
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        log.info("Found payment: {}", payment.getId());

        // 2. Dùng mapper để chuyển Entity sang DTO và trả về
        return PaymentMapper.toPaymentResponse(payment);
    }

    @Override
    public PaymentResponseDTO UpdatePaymentAsync(String id, PaymentStatusUpdateRequest request) {
        log.info("Attempting to update status for payment id: {} to {}", id, request.newStatus());

        // 1. Tìm payment bằng ID
        Payment paymentToUpdate = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        // 2. Chuyển đổi String sang ENUM (quan trọng!)
        Status newStatus;
        try {
            // .valueOf() sẽ ném IllegalArgumentException nếu String không khớp
            // ví dụ: "SHIPPED"
            newStatus = Status.valueOf(request.newStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value provided: {}", request.newStatus());
            throw new IllegalArgumentException("Invalid status value: " + request.newStatus());
        }

        // 3. Cập nhật status cho entity
        paymentToUpdate.setStatus(newStatus);

        // 4. Lưu lại vào DB.
        Payment updatedPayment = paymentRepository.save(paymentToUpdate);

        log.info("Successfully updated status for payment id: {}", updatedPayment.getId());

        // 5. Trả về DTO đã được cập nhật
        return PaymentMapper.toPaymentResponse(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true) // Dùng readOnly cho hàm GET
    public PaymentResponseDTO GetPaymentByBookingIdAsync(String bookingId) {
        log.info("Fetching payments with bookingId: {}", bookingId);

        // 1. Gọi hàm mới từ repository
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found with bookingId: " + bookingId));

        log.info("Found payment (ID: {}) for bookingId: {}", payment.getId(), bookingId);
        // 3. Map Entity duy nhất này sang DTO và trả về
        return PaymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequestBody requestBody) throws Exception {

        // BƯỚC 1: TẠO ORDER CODE
        long orderCode = System.currentTimeMillis() / 1000;
        log.info("New payment link request for orderCode: {}", orderCode);

        // BƯỚC 2: LƯU GIAO DỊCH (LẦN 1 - TẠO MỚI)
        Payment newPayment = Payment.builder()
                .bookingId(requestBody.getBookingId())
                .orderCode(orderCode)
                .amount(requestBody.getPrice())
                .description(requestBody.getDescription()) // Lấy description ban đầu
                .status(Status.PENDING) // Trạng thái chờ thanh toán
                .paymentDate(LocalDateTime.now())
                // .bookingId() // Bạn có thể thêm bookingId của bạn ở đây nếu cần
                .build();

        // Lưu vào DB và lấy đối tượng đã được quản lý (managed entity)
        Payment savedPayment = paymentRepository.save(newPayment);
        log.info("Saved PENDING payment to DB for orderCode: {}", orderCode);

        // BƯỚC 3: TẠO DATA GỬI CHO PAYOS
        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(requestBody.getProductName())
                .quantity(1)
                .price((long) requestBody.getPrice())
                .build();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode) // Đảm bảo dùng orderCode đã tạo
                .description(requestBody.getDescription())
                .amount((long) requestBody.getPrice())
                .item(item)
                .returnUrl(requestBody.getReturnUrl())
                .cancelUrl(requestBody.getCancelUrl())
                .build();

        // BƯỚC 4: GỌI PAYOS VÀ LẤY KẾT QUẢ
        CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
        log.info("PayOS link created successfully: {}", data.getCheckoutUrl());

        // BƯỚC 5: (LOGIC MỚI TỪ MẪU C#) CẬP NHẬT LẠI PAYMENT
        // Cập nhật 'description' từ PayOS trả về
        savedPayment.setDescription(data.getDescription());

        // (Không cần set Status.PENDING, vì nó đã PENDING)

        // LƯU LẦN 2 (CẬP NHẬT)
        // Vì 'savedPayment' là một đối tượng managed,
        // @Transactional sẽ tự động lưu lại khi hàm kết thúc.
        // Tuy nhiên, gọi .save() rõ ràng cũng không sao.
        paymentRepository.save(savedPayment);
        log.info("Updated payment {} with description from PayOS.", savedPayment.getId());

        // BƯỚC 6: TRẢ VỀ KẾT QUẢ CHO CLIENT
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // Đảm bảo transaction
    public void UpdatePaymentStatusByWebhook(WebhookData data) {

        // Bạn có thể kiểm tra "code" (VD: "00") hoặc "desc"
        if (data.getCode().equals("00")) {

            long orderCode = data.getOrderCode();
            log.info("Webhook processing for successful payment. Order Code: {}", orderCode);

            // 1. Tìm payment trong DB bằng orderCode
            Payment payment = paymentRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Webhook Error: Payment not found with orderCode: " + orderCode));

            // 2. Chỉ cập nhật nếu đang là PENDING
            if (payment.getStatus() == Status.PENDING) {
                payment.setStatus(Status.COMPLETED); // <-- CẬP NHẬT THÀNH CÔNG
                paymentRepository.save(payment);
                log.info("Payment status updated to COMPLETED for order code: {}", orderCode);
            } else {
                log.warn("Payment with orderCode {} was already processed. Current status: {}", orderCode, payment.getStatus());
            }
        } else {
            // Xử lý các trường hợp thanh toán thất bại (FAILED, CANCELLED) nếu muốn
            log.warn("Webhook received with non-success status: {}", data.getDesc());
        }
    }
}
