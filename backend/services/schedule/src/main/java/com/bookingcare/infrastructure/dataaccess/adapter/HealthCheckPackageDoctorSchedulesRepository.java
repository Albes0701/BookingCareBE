package com.bookingcare.infrastructure.dataaccess.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import com.bookingcare.application.ports.output.IHealthCheckPackageDoctorSchedulesRepository;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleDoctor;
import com.bookingcare.infrastructure.dataaccess.entity.HealthCheckPackageScheduleDoctorJpaEntity;
import com.bookingcare.infrastructure.dataaccess.mapper.ScheduleInfrastructureMapper;
import com.bookingcare.infrastructure.dataaccess.repository.IHealthCheckPackageScheduleDoctorJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HealthCheckPackageDoctorSchedulesRepository implements IHealthCheckPackageDoctorSchedulesRepository {
    private final IHealthCheckPackageScheduleDoctorJpaRepository _healthCheckPackageScheduleDoctorJpaRepository;
    private final ScheduleInfrastructureMapper mapper;

    @Override
    public List<HealthCheckPackageScheduleDoctor> findByDoctorId(String doctorId) {
        return _healthCheckPackageScheduleDoctorJpaRepository
                .findByDoctorId(doctorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<HealthCheckPackageScheduleDoctor> findByPackageScheduleIdAndDoctorId(String packageScheduleId,
            String doctorId) {
        return _healthCheckPackageScheduleDoctorJpaRepository
                .findByPackageScheduleIdAndDoctorId(packageScheduleId, doctorId)
                .map(mapper::toDomain);
    }

    @Override
    public HealthCheckPackageScheduleDoctor save(HealthCheckPackageScheduleDoctor doctorSchedule) {
        HealthCheckPackageScheduleDoctorJpaEntity jpaEntity = mapper.toJpaEntity(doctorSchedule);

        HealthCheckPackageScheduleDoctorJpaEntity saved = _healthCheckPackageScheduleDoctorJpaRepository
                .save(jpaEntity);

        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        try {
            _healthCheckPackageScheduleDoctorJpaRepository.deleteById(id);
            log.info("Successfully deleted doctor schedule assignment: {}", id);
        } catch (Exception e) {
            log.error("Error deleting doctor schedule assignment: {}", id, e);
            throw new RuntimeException("Failed to delete assignment", e);
        }
    }

    @Override
    public List<HealthCheckPackageScheduleDoctor> findByPackageScheduleId(String packageScheduleId) {
        return _healthCheckPackageScheduleDoctorJpaRepository
                .findByPackageScheduleId(packageScheduleId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }


    @Override
    public List<HealthCheckPackageScheduleDoctor> findByDoctorIdAndScheduleDateAndTimeSlot(
            String doctorId, 
            LocalDate scheduleDate, 
            String startTime, 
            String endTime) {
        try {
            return _healthCheckPackageScheduleDoctorJpaRepository
                    .findByDoctorIdAndScheduleDateAndTimeSlot(doctorId, scheduleDate, startTime, endTime)
                    .stream()
                    .map(jpaEntity -> mapper.toDomain(jpaEntity))
                    .toList();
        } catch (Exception e) {
            log.error("Error finding conflicting assignments for doctor: {} on date: {}", doctorId, scheduleDate, e);
            return List.of();
        }
    }




}
