package com.bookingcare.expertise.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctor_credentials",
       indexes = {
         @Index(name = "idx_dc_doctor_id", columnList = "doctor_id"),
         @Index(name = "idx_dc_type_id", columnList = "credential_type_id"),
         @Index(name = "idx_dc_status", columnList = "status"),
         @Index(name = "idx_dc_expiry", columnList = "expiry_date")
       },
       uniqueConstraints = {
         @UniqueConstraint(name = "uk_doctor_license",
                           columnNames = {"doctor_id", "credential_type_id", "license_number"})
       })
@SQLRestriction("is_deleted = false") // Hibernate 6+: thay cho @Where
public class DoctorCredential {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "doctor_id", nullable = false)
  private Doctors doctor;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "credential_type_id", nullable = false)
  private CredentialType credentialType;

  @NotBlank
  @Size(max = 128)
  @Column(name = "license_number", nullable = false, length = 128)
  private String licenseNumber;

  @NotBlank
  @Size(max = 255)
  @Column(name = "issuer", nullable = false, length = 255)
  private String issuer;

  @Size(max = 2)
  @Column(name = "country_code", length = 2)
  private String countryCode;

  @Size(max = 128)
  @Column(name = "region", length = 128)
  private String region;

  @Column(name = "issue_date")
  private LocalDate issueDate;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CredentialStatus status = CredentialStatus.PENDING;

  
  @Column(name = "note")
  private String note;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private boolean deleted = false;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  // Quan hệ 1:N tới file & log (CASCADE theo nghiệp vụ, orphanRemoval để dọn sạch link mồ côi)
  @OneToMany(mappedBy = "doctorCredential", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DoctorCredentialFile> files;

  @OneToMany(mappedBy = "doctorCredential", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DoctorCredentialVerification> verifications;
}
