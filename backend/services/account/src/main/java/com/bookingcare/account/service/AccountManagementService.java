package com.bookingcare.account.service;

import com.bookingcare.account.dto.AccountAdminDTO;
import com.bookingcare.account.entity.Accounts;
import com.bookingcare.account.entity.Users;
import com.bookingcare.account.repository.AccountsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountManagementService {

    private final AccountsRepo accountsRepo;

    @Transactional(readOnly = true)
    public List<AccountAdminDTO> getAllAccounts() {
        log.info("Fetching all accounts for admin management");
        return accountsRepo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public void softDeleteAccount(String accountId) {
        log.info("Soft deleting account with id {}", accountId);
        Accounts account = accountsRepo.findByIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found or already deleted: " + accountId));

        account.setDeleted(true);
        accountsRepo.save(account);
    }

    private AccountAdminDTO mapToDto(Accounts account) {
        Users user = account.getUser();
        return new AccountAdminDTO(
                account.getId(),
                account.getUsername(),
                account.getRoles() != null ? account.getRoles().getName() : null,
                account.isDeleted(),
                user != null ? user.getId() : null,
                user != null ? user.getFullname() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null
        );
    }
}
