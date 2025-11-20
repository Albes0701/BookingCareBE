package com.bookingcare.expertise.security;

import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bookingcare.expertise.exception.ApiException;
import com.bookingcare.expertise.exception.ErrorCode;

@Component
public class CurrentUserService {

    public String requireCurrentUserId() {
        return findCurrentUserId()
                .orElseThrow(() -> new ApiException(ErrorCode.ACCESS_DENIED));
    }

    public String requireCurrentUserIdWithRole(String requiredRole) {
        return findCurrentUserIdWithRole(requiredRole)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCESS_DENIED));
    }

    public Optional<String> findCurrentUserIdWithRole(String requiredRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        String normalizedRole = normalizeRole(requiredRole);
        boolean hasRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> normalizedRole.equals(authority));
        if (!hasRole) {
            return Optional.empty();
        }

        return extractUserId(authentication);
    }

    public Optional<String> findCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        return extractUserId(authentication);
    }

    private Optional<String> extractUserId(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailsMap) {
            Object userId = detailsMap.get("userId");
            if (userId instanceof String userIdString && StringUtils.hasText(userIdString)) {
                return Optional.of(userIdString);
            }
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString && StringUtils.hasText(principalString)) {
            return Optional.of(principalString);
        }

        return Optional.empty();
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private String normalizeRole(String requiredRole) {
        return requiredRole != null && requiredRole.startsWith("ROLE_")
                ? requiredRole
                : "ROLE_" + requiredRole;
    }
}
