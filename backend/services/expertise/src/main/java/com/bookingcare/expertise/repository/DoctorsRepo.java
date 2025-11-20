package com.bookingcare.expertise.repository;

import com.bookingcare.expertise.entity.Doctors;

import java.util.Optional;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorsRepo extends JpaRepository<Doctors,UUID> {
    Optional<Doctors> findBySlug(String slug);
    Optional<Doctors> findByUserId(String userId);

    Optional<Doctors> findByIdAndDeletedFalse(UUID id);

}
