package com.bookingcare.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;

import java.util.Date;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtReactiveAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        if (token == null || token.isBlank()) {
            return Mono.empty();
        }

        try {
            Claims claims = jwtService.extractAllClaims(token);

            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                return Mono.error(new BadCredentialsException("Token expired"));
            }

            String username = claims.getSubject();
            // Lấy userId từ claim (đã thêm ở account service); fallback sang subject nếu cần
            String userId = claims.get("userId", String.class);
            if (userId == null || userId.isBlank()) {
                userId = username;
            }

            String authoritiesClaim = claims.get("authorities", String.class);

            var authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim);
            User principal = new User(username, "", authorities);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principal, token, authorities);
            authenticationToken.setDetails(claims);

            return Mono.just(authenticationToken);
        } catch (Exception ex) {
            return Mono.error(new BadCredentialsException("Invalid token", ex));
        }
    }
}
