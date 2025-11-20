package com.bookingcare.infrastructure.dataaccess.entity;

import java.util.List;

import com.bookingcare.domain.valueobject.DayId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "schedules")
public class ScheduleJpaEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayId dayId;

    @OneToMany(mappedBy = "schedule")
    private List<HealthCheckPackageScheduleJpaEntity> healthCheckPackageSchedules;
}
