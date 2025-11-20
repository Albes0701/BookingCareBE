package com.bookingcare.account.repository;

import com.bookingcare.account.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, String> {

    // Find active users (not deleted)
    @Query("SELECT u FROM Users u WHERE u.isDeleted = false")
    List<Users> findAllActiveUsers();

    // Find user by email (active only)
    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.isDeleted = false")
    Optional<Users> findByEmailAndNotDeleted(@Param("email") String email);

    // Find user by phone (active only)
    @Query("SELECT u FROM Users u WHERE u.phone = :phone AND u.isDeleted = false")
    Optional<Users> findByPhoneAndNotDeleted(@Param("phone") String phone);

    // Find active user by ID
    @Query("SELECT u FROM Users u WHERE u.id = :id AND u.isDeleted = false")
    Optional<Users> findByIdAndNotDeleted(@Param("id") String id);

//    Users findByIdAndIsDeletedFalse(String id);

    // Check if email exists (for validation)
    boolean existsByEmailAndIsDeletedFalse(String email);

    // Check if phone exists (for validation)
    boolean existsByPhoneAndIsDeletedFalse(String phone);
}
