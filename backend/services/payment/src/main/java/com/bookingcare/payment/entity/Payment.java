package com.bookingcare.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "booking_id", unique = true)
    private String bookingId;
    @Column(name = "order_code", unique = true, nullable = false)
    private long orderCode;
    @Column(name = "amount", nullable = false)
    private long amount;
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}
