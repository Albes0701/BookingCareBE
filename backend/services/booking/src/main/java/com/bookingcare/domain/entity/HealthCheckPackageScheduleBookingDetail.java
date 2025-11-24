package com.bookingcare.domain.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

import com.bookingcare.domain.valueobject.BookingStatus;
import com.bookingcare.domain.valueobject.PurchaseMethod;

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
  private String patientRelativesName;
  private String patientRelativesPhoneNumber;
  private String patientId;
  private String packageScheduleId;
  private String bookingPackageId;
  private String bookingReason;
  private String clinicId;
  private String doctorId;
  private BookingStatus bookingStatus;
  private PurchaseMethod purchaseMethod;
  
  private ZonedDateTime createdDate;
  private ZonedDateTime updatedDate;

  // Relationships
  private BookingPackage bookingPackage;
  private BookingPackageDetail bookingPackageDetail;
  private BookingSagaState sagaState;  // Reference to saga orchestration state

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
    this.bookingStatus = BookingStatus.PENDING;
    this.createdDate = ZonedDateTime.now();
    this.updatedDate = ZonedDateTime.now();
  }
  
  // Saga state transition methods
  public void confirmHoldSchedule(String scheduleHoldId, ZonedDateTime holdExpireAt) {
    this.bookingStatus = BookingStatus.PENDING_PAYMENT;
    this.updatedDate = ZonedDateTime.now();
    // Saga state is updated separately in BookingSagaState entity
  }
  
  public void failHoldSchedule() {
    this.bookingStatus = BookingStatus.REJECTED_NO_SLOT;
    this.updatedDate = ZonedDateTime.now();
  }
  
  public void confirmPayment() {
    this.updatedDate = ZonedDateTime.now();
    // Payment status is tracked in BookingSagaState entity
  }
  
  public void failPayment() {
    this.bookingStatus = BookingStatus.CANCELLED;
    this.updatedDate = ZonedDateTime.now();
  }
  
  public void confirmBooking() {
    this.bookingStatus = BookingStatus.CONFIRMED;
    this.updatedDate = ZonedDateTime.now();
  }
}
