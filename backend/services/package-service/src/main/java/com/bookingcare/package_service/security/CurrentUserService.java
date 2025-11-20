package com.bookingcare.package_service.security;

import com.bookingcare.package_service.exception.ApiException;
import com.bookingcare.package_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Minimal helper that resolves the current user from gateway-propagated headers.
 * This avoids pulling in the entire Spring Security stack while still letting
 * service-layer code enforce ownership constraints.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserService {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    private final HttpServletRequest request;

    public String requireCurrentUserId() {
        String userId = request.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(userId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Missing user identity context");
        }
        return userId;
    }

    public String requireCurrentUserIdWithRole(String requiredRole) {
        String userId = requireCurrentUserId();
        if (!hasRole(requiredRole)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Insufficient role");
        }
        return userId;
    }

    public boolean hasRole(String role) {
        String rolesHeader = request.getHeader(HEADER_USER_ROLES);
        if (!StringUtils.hasText(role) || !StringUtils.hasText(rolesHeader)) {
            return false;
        }
        String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .anyMatch(r -> normalized.equalsIgnoreCase(r));
    }
}
