package com.bookingcare.domain.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

import com.bookingcare.domain.valueobject.BookingStatus;
import com.bookingcare.domain.valueobject.PaymentStatus;
import com.bookingcare.domain.valueobject.PurchaseMethod;
import com.bookingcare.domain.valueobject.VisitStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckPackageScheduleBookingDetail {
  private String id;
  
  // Thông tin người thân (nếu booking hộ)
  private String patientRelativesName;
  private String patientRelativesPhoneNumber;
  
  // Thông tin liên kết domain
  private String patientId;
  private String packageScheduleId;
  private String bookingPackageId;
  private String clinicId;
  private String clinicBranchId;
  
  // Bác sĩ phụ trách ca khám này
  private String doctorId;
  
  // Lý do đặt lịch, ghi chú thêm từ bệnh nhân
  private String bookingReason;
  
  // Trạng thái booking theo lifecycle đặt lịch / thanh toán
  private BookingStatus bookingStatus;
  
  // Trạng thái thực tế của buổi khám tại phòng khám
  private VisitStatus visitStatus;
  
  // Phương thức mua / thanh toán
  private PurchaseMethod purchaseMethod;
  
  // Thông tin liên quan tới HOLD SLOT (Schedule Service)
  private String scheduleHoldId;
  private ZonedDateTime holdExpireAt;
  
  // Thông tin cache Payment
  private String externalPaymentId;
  private PaymentStatus lastPaymentStatus;
  
  // Timestamps
  private ZonedDateTime createdDate;
  private ZonedDateTime updatedDate;

  // Relationships
  private BookingPackage bookingPackage;

  public String generateId() {
    try {
      String newBookingId = packageScheduleId + patientId;

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(newBookingId.getBytes(StandardCharsets.UTF_8));

      // Convert hash bytes to hexadecimal string
      StringBuilder hexString = new StringBuilder();
      for (byte hashByte : hashBytes) {
        String hex = Integer.toHexString(0xff & hashByte);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }

      // Take first 16 characters of the hash
      return hexString.toString().substring(0, 16);
    } catch (NoSuchAlgorithmException e) {
      // Fallback to UUID if SHA-256 is not available
      return java.util.UUID.randomUUID().toString().substring(0, 16);
    }
  }

  public void initialize() {
    this.id = generateId();
    this.bookingStatus = BookingStatus.PENDING_SCHEDULE;
    this.createdDate = ZonedDateTime.now();
    this.updatedDate = ZonedDateTime.now();
  }
  
  public void updateTimestamp() {
    this.updatedDate = ZonedDateTime.now();
  }
}
