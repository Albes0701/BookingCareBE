package com.bookingcare.account.controller;

import com.bookingcare.account.dto.AccountAdminDTO;
import com.bookingcare.account.service.AccountManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AccountManagementController {

    private final AccountManagementService accountManagementService;

    @GetMapping
    public ResponseEntity<List<AccountAdminDTO>> getAccounts() {
        List<AccountAdminDTO> accounts = accountManagementService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Map<String, String>> softDeleteAccount(@PathVariable String accountId) {
        accountManagementService.softDeleteAccount(accountId);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}
