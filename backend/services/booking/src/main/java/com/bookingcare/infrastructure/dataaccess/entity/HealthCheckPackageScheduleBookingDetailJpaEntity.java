package com.bookingcare.infrastructure.dataaccess.entity;

import java.time.ZonedDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.bookingcare.domain.valueobject.BookingStatus;
import com.bookingcare.domain.valueobject.PurchaseMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "health_check_package_schedule_booking_details")
public class HealthCheckPackageScheduleBookingDetailJpaEntity {

  @Id
  private String id;

  @Column(nullable = true)
  private String patientRelativesName;

  @Column(nullable = true)
  private String patientRelativesPhoneNumber;

  @Column(nullable = false)
  private String patientId;

  @ManyToOne
  @JoinColumn(name = "patientId", nullable = false, insertable = false, updatable = false)
  private PatientJpaEntity patient;

  @Column(nullable = false)
  private String packageScheduleId;

  @Column(nullable = false)
  private String bookingPackageId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "bookingPackageId", nullable = false, insertable = false, updatable = false)
  private BookingPackageJpaEntity bookingPackage;

  @Column(nullable = true)
  private String bookingReason;

  @Column(nullable = false)
  private String clinicId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private BookingStatus bookingStatus;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PurchaseMethod purchaseMethod;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private ZonedDateTime createdDate;

  @LastModifiedDate
  @Column(nullable = false)
  private ZonedDateTime updatedDate;
}
