package com.bookingcare.infrastructure.dataaccess.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.infrastructure.dataaccess.entity.ScheduleJpaEntity;

public interface IScheduleJpaRepository extends JpaRepository<ScheduleJpaEntity, String> {
    Optional<ScheduleJpaEntity> findById(String id);
}
