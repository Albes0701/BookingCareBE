package com.bookingcare.expertise.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "credential_types",
       indexes = {
         @Index(name = "idx_credential_types_code", columnList = "code")
       },
       uniqueConstraints = {
         @UniqueConstraint(name = "uk_credential_types_code", columnNames = {"code"})
       })
public class CredentialType {

  @Id
  @GeneratedValue
  private UUID id;

  @NotBlank
  @Size(max = 64)
  @Column(name = "code", nullable = false, unique = true, length = 64)
  private String code;

  @NotBlank
  @Size(max = 255)
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  
  @Column(name = "description")
  private String description;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private boolean is_deleted = true;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;
}
