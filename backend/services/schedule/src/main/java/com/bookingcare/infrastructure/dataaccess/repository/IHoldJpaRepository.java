package com.bookingcare.infrastructure.dataaccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.infrastructure.dataaccess.entity.ScheduleHoldJpaEntity;

public interface IHoldJpaRepository extends JpaRepository<ScheduleHoldJpaEntity, String> {
    
}
