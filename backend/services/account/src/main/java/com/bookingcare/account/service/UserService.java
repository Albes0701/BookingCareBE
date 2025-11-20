package com.bookingcare.account.service;

import com.bookingcare.account.client.ExpertiseClient;
import com.bookingcare.account.dto.UpdateProfileRequest;
import com.bookingcare.account.dto.UserDTO;
import com.bookingcare.account.entity.Users;
import com.bookingcare.account.mapper.UsersMapper;
import com.bookingcare.account.repository.UsersRepo;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import main.java.com.bookingcare.shared.dto.expertise.DoctorsResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import com.bookingcare.account.entity.Users;
import com.bookingcare.account.exception.ApiException;
import com.bookingcare.account.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UsersRepo usersRepo;
    private final UsersMapper usersMapper;
    private final ExpertiseClient expertiseClient;

    /**
     * Get all active users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all active users");

        List<Users> users = usersRepo.findAllActiveUsers();
        return users.stream()
                .map(usersMapper::toUsersDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        log.info("Fetching user with ID: {}", id);

        Users user = usersRepo.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        return usersMapper.toUsersDTO(user);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);

        Users user = usersRepo.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return usersMapper.toUsersDTO(user);
    }

    /**
     * Update user details
     */
    public UserDTO updateUser(String id, UpdateProfileRequest request) {
        log.info("Updating user with ID: {}", id);

        Users existingUser = usersRepo.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Validate email uniqueness if email is being changed
        if (request.email() != null && !request.email().equals(existingUser.getEmail())) {
            if (usersRepo.existsByEmailAndIsDeletedFalse(request.email())) {
                throw new RuntimeException("Email already exists: " + request.email());
            }
        }

        // Validate phone uniqueness if phone is being changed
        if (request.phone() != null && !request.phone().equals(existingUser.getPhone())) {
            if (usersRepo.existsByPhoneAndIsDeletedFalse(request.phone())) {
                throw new RuntimeException("Phone number already exists: " + request.phone());
            }
        }

        // Update the user using mapper
        usersMapper.updateUsersFromRequest(request, existingUser);
        Users updatedUser = usersRepo.save(existingUser);

        log.info("Successfully updated user with ID: {}", id);
        return usersMapper.toUsersDTO(updatedUser);
    }

    /**
     * Soft delete user (set isDeleted = true)
     */
    public void deleteUser(String id) {
        log.info("Soft deleting user with ID: {}", id);

        Users user = usersRepo.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setDeleted(true);
        usersRepo.save(user);

        log.info("Successfully soft deleted user with ID: {}", id);
    }

    // * Get currently authenticated user

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        String userId = (String) details.get("userId");
        if (!StringUtils.hasText(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "Missing userId in security context");
        }
        return userId;
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        String userId = currentUserId();

        Users user = usersRepo.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return usersMapper.toUsersDTO(user);
    }

    public Optional<com.bookingcare.account.dto.DoctorsResponseDTO> getDoctorForCurrentUser() {
        String userId = currentUserId(); // reuse helper from UserService
        try {
            return Optional.of(expertiseClient.getDoctorByUserId(userId));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
