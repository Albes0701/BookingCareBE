package com.bookingcare.clinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "clinics", indexes = {
        @Index(name = "idx_clinics_created_by_user_id", columnList = "created_by_user_id")
})
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "fullname", length = 255)
    private String fullname;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "clinic_detail_info", columnDefinition = "TEXT")
    private String clinicDetailInfo;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "slug", unique = true, length = 255)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ClinicStatus status;

    @Column(name = "created_by_user_id", nullable = false, length = 36, updatable = false)
    private String createdByUserId;

    @Column(name = "is_deleted", nullable = false, updatable = false)
    private boolean isDeleted;

}


