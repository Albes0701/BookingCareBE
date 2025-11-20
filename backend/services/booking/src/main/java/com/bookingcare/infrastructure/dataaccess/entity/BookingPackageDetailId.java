package com.bookingcare.infrastructure.dataaccess.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingPackageDetailId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "booking_package_id", nullable = false)
    private String bookingPackageId;

    @Column(name = "package_id", nullable = false)
    private String packageId;
}