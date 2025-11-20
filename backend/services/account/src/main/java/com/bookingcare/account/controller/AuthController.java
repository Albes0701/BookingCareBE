package com.bookingcare.account.controller;

import com.bookingcare.account.dto.AuthRequest;
import com.bookingcare.account.dto.AuthResponse;
import com.bookingcare.account.dto.ChangePasswordRequest;
import com.bookingcare.account.dto.RegisterRequest;
import com.bookingcare.account.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account/auth") // nhóm API
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
//        AuthResponse response = authService.register(registerRequest);
//        return ResponseEntity.ok(response);
//
//    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Log the error and return appropriate response
            return ResponseEntity.badRequest().build();
        }
    }



    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        // 1. Lấy username từ người dùng đã đăng nhập (cách làm bảo mật)
        String username = authentication.getName();

        // 2. Gọi service để thực hiện logic nghiệp vụ
        authService.changePassword(username, request);

        // 3. Trả về response thành công
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register/doctor")
    public ResponseEntity<AuthResponse> registerDoctor(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request, "DOCTOR");
        return ResponseEntity.ok(response);
    }

   





}
