package com.bookingcare.infrastructure.dataaccess.adapter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bookingcare.application.ports.output.IScheduleHoldRepository;
import com.bookingcare.domain.entity.ScheduleHold;
import com.bookingcare.infrastructure.dataaccess.mapper.ScheduleInfrastructureMapper;
import com.bookingcare.infrastructure.dataaccess.repository.IHoldJpaRepository;

@Repository
public class ScheduleHoldRepository implements IScheduleHoldRepository{

    private final IHoldJpaRepository _holdJpaRepository;
    private final ScheduleInfrastructureMapper mapper;

    @Autowired
    public ScheduleHoldRepository(IHoldJpaRepository holdJpaRepository, ScheduleInfrastructureMapper mapper) {
        this._holdJpaRepository = holdJpaRepository;
        this.mapper = mapper;
    }



    @Override
    public ScheduleHold save(ScheduleHold scheduleHold) {
        var jpaEntity = mapper.toJpaEntity(scheduleHold);
        var savedEntity = _holdJpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ScheduleHold> findById(String id) {
        return _holdJpaRepository
                .findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(ScheduleHold scheduleHold) {
        var jpaEntity = mapper.toJpaEntity(scheduleHold);
        _holdJpaRepository.delete(jpaEntity);
    }
    
}
