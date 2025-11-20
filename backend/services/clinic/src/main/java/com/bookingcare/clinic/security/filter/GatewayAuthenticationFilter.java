package com.bookingcare.clinic.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Maps propagated gateway headers to the Spring Security context so downstream layers
 * can resolve the authenticated user without parsing JWTs again.
 */
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_ID = "X-User-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USER_NAME);

        if (StringUtils.hasText(userId) && StringUtils.hasText(username)) {
            String rolesHeader = request.getHeader(HEADER_USER_ROLES);
            Authentication authentication = buildAuthentication(userId, username, rolesHeader);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Authentication buildAuthentication(String userId, String username, String rolesHeader) {
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(
                            StringUtils.hasText(rolesHeader) ? rolesHeader : ""
                    )
            );
            token.setDetails(Map.of(
                    "userId", userId,
                    "username", username,
                    "roles", StringUtils.hasText(rolesHeader) ? rolesHeader : ""
            ));
            return token;
        } catch (Exception ex) {
            throw new BadCredentialsException("Unable to map propagated authentication headers", ex);
        }
    }
}
