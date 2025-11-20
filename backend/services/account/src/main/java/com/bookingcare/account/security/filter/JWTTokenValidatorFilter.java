package com.bookingcare.account.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_ID= "X-User-Id";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/v1/account/auth/login",
        "/api/v1/account/auth/register"
    );

    public JWTTokenValidatorFilter() {}

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String username = request.getHeader(HEADER_USER_NAME);
        String userId   = request.getHeader(HEADER_USER_ID);

        if (StringUtils.hasText(username)) {
            String rolesHeader = request.getHeader(HEADER_USER_ROLES);
            String authorities = StringUtils.hasText(rolesHeader) ? rolesHeader : "";

            try {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(authorities)
                );
                // lưu thêm thông tin bổ sung để service layer có thể đọc
                token.setDetails(Map.of(
                        "userId", userId,
                        "username", username,
                        "roles", authorities
                ));
                SecurityContextHolder.getContext().setAuthentication(token);
            } catch (Exception e) {
                throw new BadCredentialsException("Invalid propagated authentication headers", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_ENDPOINTS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
