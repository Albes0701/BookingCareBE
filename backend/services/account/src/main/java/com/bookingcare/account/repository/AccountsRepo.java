package com.bookingcare.account.repository;

import com.bookingcare.account.entity.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountsRepo extends JpaRepository<Accounts, String> {

    Accounts findByUsername(String username);


    Optional<Accounts> findByIdAndIsDeletedFalse(String id);
}
