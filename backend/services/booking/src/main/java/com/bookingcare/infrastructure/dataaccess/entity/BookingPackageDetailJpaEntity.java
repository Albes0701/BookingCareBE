package com.bookingcare.infrastructure.dataaccess.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
@Table(name = "booking_packages_details")
public class BookingPackageDetailJpaEntity {
    @EmbeddedId
    private BookingPackageDetailId id;

    @ManyToOne
    @MapsId("bookingPackageId")
    @JoinColumn(name = "booking_package_id", nullable = false)
    private BookingPackageJpaEntity bookingPackage;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "description", nullable = true)
    private String description;
}
