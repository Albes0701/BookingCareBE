package com.bookingcare.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", updatable = false, nullable = false)
//    private String id;
//    @Column(name = "refresh_token", nullable = false)
//    private String refreshToken;
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private Users user;
//    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
//    private boolean deleted;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;
}
