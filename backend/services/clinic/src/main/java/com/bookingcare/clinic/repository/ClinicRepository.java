package com.bookingcare.clinic.repository;

import com.bookingcare.clinic.entity.Clinic;
import com.bookingcare.clinic.entity.ClinicStatus;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClinicRepository extends JpaRepository<Clinic, String> {

    Optional<Clinic> findByIdAndStatusAndIsDeletedFalse(String id, ClinicStatus status);

    Optional<Clinic> findByIdAndIsDeletedFalse(String id);

    Optional<Clinic> findBySlugAndStatusAndIsDeletedFalse(String slug, ClinicStatus status);

    @Query("""
            SELECT c
            FROM Clinic c
            WHERE c.status = :status
              AND c.isDeleted = false
              AND (:q IS NULL
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.fullname) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Clinic> findByStatusAndSearch(
            @Param("status") ClinicStatus status,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM Clinic c
            WHERE c.createdByUserId = :ownerId
              AND c.isDeleted = false
              AND (:status IS NULL OR c.status = :status)
              AND (:q IS NULL
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.fullname) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.address) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Clinic> findByOwnerAndFilters(
            @Param("ownerId") String ownerId,
            @Param("status") ClinicStatus status,
            @Param("q") String q,
            Pageable pageable
    );
}
