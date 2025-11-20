package com.bookingcare.expertise.repository;

import com.bookingcare.expertise.entity.Doctors_Specialties;
import com.bookingcare.expertise.entity.Specialties;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Doctor_Specialty_Repo extends JpaRepository<Doctors_Specialties,UUID> {

    List<Doctors_Specialties> findAllBySpecialties(Specialties specialties);

    Optional<Doctors_Specialties> findByDoctorsIdAndSpecialtiesId(UUID doctorId, UUID specialtyId);

}
