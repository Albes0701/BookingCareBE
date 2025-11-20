package com.bookingcare.account.service;

import com.bookingcare.account.dto.*;
import com.bookingcare.account.entity.Accounts;
import com.bookingcare.account.entity.Roles;
import com.bookingcare.account.entity.Users;
import com.bookingcare.account.mapper.UsersMapper;
import com.bookingcare.account.repository.AccountsRepo;
import com.bookingcare.account.repository.RolesRepo;
import com.bookingcare.account.repository.UsersRepo;
import jakarta.transaction.Transactional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final AccountsRepo accountsRepo;
    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final UsersMapper usersMapper;
    private final RolesRepo rolesRepo;
    private final JwtService jwtService;

    public AuthService(AccountsRepo accountsRepo, UsersRepo usersRepo, PasswordEncoder passwordEncoder, UsersMapper usersMapper, RolesRepo rolesRepo, JwtService jwtService) {
        this.accountsRepo = accountsRepo;
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.usersMapper = usersMapper;
        this.rolesRepo = rolesRepo;
        this.jwtService = jwtService;
    }

    // Login
    @Transactional
    public AuthResponse login(AuthRequest request) {
        // Input validation
        validateLoginRequest(request);

        // Find account by username
        Accounts account = accountsRepo.findByUsername(request.username().trim());
        if (account == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Validate account and user status
        validateAccountStatus(account);

        // Verify password
        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate tokens and build response
        return generateAuthResponse(account);
    }

    private void validateLoginRequest(AuthRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new BadCredentialsException("Username cannot be empty");
        }
        if (request.password() == null || request.password().isEmpty()) {
            throw new BadCredentialsException("Password cannot be empty");
        }
    }

    private void validateAccountStatus(Accounts account) {
        // Check if account is soft deleted
        if (account.isDeleted()) {
            throw new BadCredentialsException("Account is no longer active");
        }

        // Check if user exists and is active
        Users user = account.getUser();
        if (user == null) {
            throw new BadCredentialsException("User profile not found");
        }
        if (user.isDeleted()) {
            throw new BadCredentialsException("User profile is no longer active");
        }
    }

    private AuthResponse generateAuthResponse(Accounts account) {
        try {
            // Generate JWT tokens using JwtService
            String accessToken = jwtService.generateAccessToken(account);
            String refreshToken = jwtService.generateRefreshToken(account);

            // Map user to DTO
            UserDTO userDTO = usersMapper.toUsersDTO(account.getUser());

            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    account.getRoles().getName(),
                    userDTO
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate authentication tokens", e);
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return register(request, "PATIENT");
    }

    // Register
    @Transactional
    public AuthResponse register(RegisterRequest request,String roleName) {

        // Validate input
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        // 1. Kiểm tra username đã tồn tại chưa
        Accounts existingAccount = accountsRepo.findByUsername(request.username());
        if (existingAccount != null) {
            throw new RuntimeException("Username already exists");
        }

        Roles role=rolesRepo.findByName(roleName);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }

        // 2. Tạo mới user
        Users newUser = new Users();
        newUser.setFullname(request.fullName());
        Users savedUser = usersRepo.save(newUser);

        // 3. Tạo mới account
        Accounts newAccount = new Accounts();
        newAccount.setUsername(request.username());
        newAccount.setPassword(passwordEncoder.encode(request.password()));
        newAccount.setUser(savedUser);
        newAccount.setRoles(role);
        accountsRepo.save(newAccount);

        // 4. Map sang UserDTO
        UserDTO userDTO = usersMapper.toUsersDTO(savedUser);

        // 5. Tạo token thật
        String accessToken = jwtService.generateAccessToken(newAccount);
        String refreshToken = jwtService.generateRefreshToken(newAccount);

        // 6. Trả về AuthResponse (tạm dùng dummy token)
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                newAccount.getRoles().getName(),
                userDTO
        );

   }


    // Change Password
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        // 1. Tìm account và xử lý trường hợp không tồn tại ngay lập tức
        // Sử dụng orElseThrow để code gọn gàng hơn
        Accounts account = accountsRepo.findByUsername(username);

        if(account==null){
            throw new RuntimeException("Invalid username or password");
        }

        // Optional: Kiểm tra account có bị khóa hay soft-delete không
        if (account.isDeleted()) {
            throw new RuntimeException("Invalid username or password"); // Thông điệp chung chung
        }

        // 2. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.oldPassword(), account.getPassword())) {
            throw new RuntimeException("Invalid username or password"); // Thông điệp chung chung, an toàn hơn
        }

        // 3. (Optional nhưng khuyến khích) Validate mật khẩu mới
        if (passwordEncoder.matches(request.newPassword(), account.getPassword())) {
            throw new RuntimeException("New password cannot be the same as the old password.");
        }

        // Thêm các logic validate độ mạnh mật khẩu khác ở đây nếu cần
        // validatePasswordStrength(newPassword);

        // 4. Cập nhật mật khẩu mới
        account.setPassword(passwordEncoder.encode(request.newPassword()));
        accountsRepo.save(account);
    }


    
    







}
