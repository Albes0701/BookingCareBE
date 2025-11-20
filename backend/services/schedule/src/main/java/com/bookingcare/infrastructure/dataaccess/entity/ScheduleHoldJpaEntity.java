package com.bookingcare.infrastructure.dataaccess.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "schedule_holds")
public class ScheduleHoldJpaEntity {
    @Id
    private String id;

    @Column(name = "package_schedule_id", nullable = false)
    private String packageScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_schedule_id", insertable = false, updatable = false)
    private HealthCheckPackageScheduleJpaEntity packageSchedule;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "expire_at", nullable = false)
    private ZonedDateTime expireAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
