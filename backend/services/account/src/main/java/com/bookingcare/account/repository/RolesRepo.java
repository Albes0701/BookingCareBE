package com.bookingcare.account.repository;

import com.bookingcare.account.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepo extends JpaRepository<Roles, String> {

    Roles findByName(String name);

}
