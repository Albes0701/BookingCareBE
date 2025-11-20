package com.bookingcare.expertise.repository;

import com.bookingcare.expertise.entity.Specialties;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepo extends JpaRepository<Specialties,UUID> {

    Optional<Specialties> findBySlug(String slug);
    Optional<Specialties> findByCodeIgnoreCase(String code);
}
