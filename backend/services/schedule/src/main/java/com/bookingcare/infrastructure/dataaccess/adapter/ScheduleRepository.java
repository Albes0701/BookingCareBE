package com.bookingcare.infrastructure.dataaccess.adapter;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bookingcare.application.ports.output.IScheduleRepository;
import com.bookingcare.domain.entity.Schedule;
import com.bookingcare.infrastructure.dataaccess.mapper.ScheduleInfrastructureMapper;
import com.bookingcare.infrastructure.dataaccess.repository.IScheduleJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ScheduleRepository implements IScheduleRepository {
    private final IScheduleJpaRepository _scheduleJpaRepository;
    private final ScheduleInfrastructureMapper mapper;

    @Override
    public Optional<Schedule> findById(String packageScheduleId) {
        return _scheduleJpaRepository
                .findById(packageScheduleId)
                .map(mapper::toDomain);
    }

}
