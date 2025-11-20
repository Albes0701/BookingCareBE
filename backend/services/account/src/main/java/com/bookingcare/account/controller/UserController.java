package com.bookingcare.account.controller;

import com.bookingcare.account.dto.UpdateProfileRequest;
import com.bookingcare.account.dto.UserDTO;
import com.bookingcare.account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/account/users")
@Slf4j
public class UserController {

    private final UserService userService;


    /**
     * Get all active users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Fetching all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        log.info("Fetching user with ID: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id,
                                            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating user with ID: {}", id);
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Soft delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
                "message", "User successfully deleted",
                "userId", id,
                "status", 200
        ));
    }



    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        log.info("Fetching current user profile");
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // Doctor profile of current user

    @GetMapping("me/doctor")
    public ResponseEntity<com.bookingcare.account.dto.DoctorsResponseDTO> getOwnDoctorProfile() {
        return userService.getDoctorForCurrentUser()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }



}
