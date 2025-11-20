package com.bookingcare.infrastructure.dataaccess.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "booking_packages")
public class BookingPackageJpaEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "bookingPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingPackageDetailJpaEntity> details = new ArrayList<>();

    @OneToMany(mappedBy = "bookingPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<HealthCheckPackageScheduleBookingDetailJpaEntity> bookingDetails = new ArrayList<>();
}
