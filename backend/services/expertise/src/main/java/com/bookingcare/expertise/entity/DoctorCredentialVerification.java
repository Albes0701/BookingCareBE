package com.bookingcare.expertise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "doctor_credential_verifications",
        indexes = {
         @Index(name = "idx_dcv_credential", columnList = "doctor_credential_id")
       })
public class DoctorCredentialVerification {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "doctor_credential_id", nullable = false)
  private DoctorCredential doctorCredential;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 20)
  private VerificationAction action;

  // Nếu sau này bạn có bảng users cho reviewer, có thể đổi sang @ManyToOne tới User
  @Column(name = "actor_id")
  private UUID actorId;

 
  @Column(name = "comment")
  private String comment;

  @Builder.Default
  @Column(name = "created_at")
  private Instant createdAt = Instant.now();
}
