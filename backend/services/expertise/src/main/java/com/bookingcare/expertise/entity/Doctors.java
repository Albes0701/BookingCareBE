package com.bookingcare.expertise.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
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
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(
        name = "doctors",
        indexes = {
                @Index(name = "idx_doctors_slug", columnList = "slug"),
                @Index(name = "idx_doctors_user_id", columnList = "user_id")
        }
)
@SQLDelete(sql = "UPDATE doctors SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Doctors {

    @Id
    @GeneratedValue
    private UUID id;

    // Nếu userId là duy nhất (1-1 với user), bật unique để tránh trùng
    @NotBlank
    @Size(max = 255)
    @Column(name = "user_id", nullable = false, unique = true, length = 255)
    private String userId;

    @Column(name = "short_doctor_infor")
    private String shortDoctorInfor;

    @Column(name = "doctor_detail_infor", columnDefinition = "TEXT")
    private String doctorDetailInfor;

    @NotBlank
    @Size(max = 255)
    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void ensureSlug() {
        if (slug == null || slug.isBlank()) {
            // Có thể tạo slug từ userId/doctorInfor/name hiển thị (tùy product)
            this.slug = slugify(this.userId);
        } else {
            this.slug = slugify(this.slug);
        }
    }

    private String slugify(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^\\p{Alnum}]+", "-");
        s = s.replaceAll("(^-+|-+$)", "");
        return s;
    }


}
