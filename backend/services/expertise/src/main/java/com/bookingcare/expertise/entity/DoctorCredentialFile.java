package com.bookingcare.expertise.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctor_credential_files",
       indexes = {
         @Index(name = "idx_dcf_credential", columnList = "doctor_credential_id")
       })
public class DoctorCredentialFile {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "doctor_credential_id", nullable = false)
  private DoctorCredential doctorCredential;

  @NotBlank
  @Size(max = 255)
  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Size(max = 100)
  @Column(name = "content_type", length = 100)
  private String contentType;


  @NotBlank
  @Column(name = "file_url", nullable = false)
  private String fileUrl;

  @Builder.Default
  @Column(name = "uploaded_at")
  private Instant uploadedAt = Instant.now();
}