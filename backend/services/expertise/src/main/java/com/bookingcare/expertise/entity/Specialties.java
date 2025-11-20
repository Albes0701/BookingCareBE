package com.bookingcare.expertise.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "specialties",
        indexes = {
                @Index(name = "idx_specialties_slug", columnList = "slug"),
                @Index(name = "idx_specialties_code", columnList = "code")
        }
)
@SQLDelete(sql = "UPDATE specialties SET is_deleted = true WHERE id = ?")
public class Specialties {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code; // giữ giá trị như "OPHTHALMOLOGY"

   
    @Column(name = "specialty_detail_infor", columnDefinition = "TEXT")
    private String specialtyDetailInfor;

    @NotBlank
    @Size(max = 255)
    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

   
    @Size(max = 2048)
    @Column(name = "image", length = 2048)
    private String image;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SpecialtyStatus status = SpecialtyStatus.DRAFT;

    @PrePersist
    @PreUpdate
    private void ensureSlug() {
        if (slug == null || slug.isBlank()) {
            this.slug = slugify(this.name);
        } else {
            this.slug = slugify(this.slug);
        }
    }

    private String slugify(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase(Locale.ROOT);
        // thay khoảng trắng và ký tự không phải chữ/số thành '-'
        s = s.replaceAll("[^\\p{Alnum}]+", "-");
        // bỏ '-' ở đầu/cuối
        s = s.replaceAll("(^-+|-+$)", "");
        return s;
    }

}
