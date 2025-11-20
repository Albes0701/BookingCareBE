package com.bookingcare.expertise.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookingcare.expertise.entity.CredentialType;

public interface CredentialTypeRepo extends JpaRepository<CredentialType,UUID> {
    
}
