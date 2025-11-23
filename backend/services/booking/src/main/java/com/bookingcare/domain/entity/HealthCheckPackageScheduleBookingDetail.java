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
  private BookingStatus bookingStatus;
  private PurchaseMethod purchaseMethod;
  private ZonedDateTime createdDate;
  private ZonedDateTime updatedDate;

  private HealthCheckPackageSchedule packageSchedule;
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
    this.bookingStatus = BookingStatus.PENDING;
  }
}
