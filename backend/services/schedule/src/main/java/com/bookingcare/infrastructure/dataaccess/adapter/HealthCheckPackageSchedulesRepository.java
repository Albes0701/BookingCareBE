package com.bookingcare.infrastructure.dataaccess.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bookingcare.application.dto.PackageDetailResponse;
import com.bookingcare.application.ports.output.IHealthCheckPackageSchedulesRepository;
import com.bookingcare.domain.entity.HealthCheckPackageSchedule;
import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleJpaEntity;
import com.bookingcare.infrastructure.dataaccess.mapper.ScheduleInfrastructureMapper;
import com.bookingcare.infrastructure.dataaccess.repository.IHealthCheckPackageScheduleJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HealthCheckPackageSchedulesRepository implements IHealthCheckPackageSchedulesRepository {
        private final IHealthCheckPackageScheduleJpaRepository _healthCheckPackageScheduleJpaRepository;
        
        private final ScheduleInfrastructureMapper mapper;

        @Override
        public Optional<HealthCheckPackageSchedule> findById(String packageScheduleId) {
                return _healthCheckPackageScheduleJpaRepository
                        .findById(packageScheduleId)
                        .map(mapper::toDomain);
        }

        @Override
        public List<HealthCheckPackageSchedule> findHealthCheckPackageSchedulesByDate(String healthCheckPackageId,
                LocalDate date) {

                return _healthCheckPackageScheduleJpaRepository
                        .findByHealthCheckPackageIdAndDate(healthCheckPackageId, date)
                        .stream()
                        .map(mapper::toDomain)
                        .sorted((a, b) -> a.getSchedule().getId().compareToIgnoreCase(b.getSchedule().getId()))
                        .toList();
        }

        @Override
        public HealthCheckPackageSchedule save(HealthCheckPackageSchedule schedule) {
                HealthCheckPackageScheduleJpaEntity jpaEntity = mapper.toJpaEntity(schedule);

                HealthCheckPackageScheduleJpaEntity saved = _healthCheckPackageScheduleJpaRepository
                        .save(jpaEntity);

                return mapper.toDomain(saved);
        }

        @Override
        public List<HealthCheckPackageSchedule> saveAll(List<HealthCheckPackageSchedule> schedules) {
                List<HealthCheckPackageScheduleJpaEntity> jpaEntities = schedules.stream()
                        .map(mapper::toJpaEntity)
                        .toList();

                List<HealthCheckPackageScheduleJpaEntity> savedEntities = _healthCheckPackageScheduleJpaRepository
                        .saveAll(jpaEntities);

                return savedEntities.stream()
                        .map(mapper::toDomain)
                        .toList();
        }

        @Override
        public Optional<HealthCheckPackageSchedule> findByHealthCheckPackageIdScheduleIdAndDate(String packageId,
                        String scheduleId, LocalDate date) {
                return _healthCheckPackageScheduleJpaRepository
                        .findByHealthCheckPackageIdScheduleIdAndDate(packageId, scheduleId, date)
                        .map(mapper::toDomain);
        }

        @Override
        public List<HealthCheckPackageSchedule> findByPackageId(String packageId) {
                return _healthCheckPackageScheduleJpaRepository
                        .findByPackageId(packageId)
                        .stream()
                        .map(mapper::toDomain)
                        .toList();
        }


        @Override
        public List<HealthCheckPackageSchedule> findByPackageIdAndScheduleDate(String packageId, LocalDate scheduleDate) {
        return _healthCheckPackageScheduleJpaRepository
                .findByPackageIdAndScheduleDate(packageId, scheduleDate)
                .stream()
                .map(mapper::toDomain)
                .toList();
        }

}