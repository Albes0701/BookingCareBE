package com.bookingcare.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

public class GatewayUserPropagationFilter implements WebFilter {

    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_TOKEN_EXPIRES_AT = "X-Token-Expires-At";
    private static final String HEADER_USER_ID = "X-User-Id";

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .flatMap(authentication -> propagateHeaders(exchange, chain, authentication))
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> propagateHeaders(ServerWebExchange exchange,
                                        WebFilterChain chain,
                                        Authentication authentication) {

        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String expiresAt = extractExpiration(authentication);
        String userId = extractUserId(authentication);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove(HEADER_USER_NAME);
                    httpHeaders.remove(HEADER_USER_ROLES);
                    httpHeaders.remove(HEADER_TOKEN_EXPIRES_AT);
                    httpHeaders.remove(HEADER_USER_ID);

                    httpHeaders.set(HEADER_USER_NAME, username);
                    if (!roles.isEmpty()) {
                        httpHeaders.set(HEADER_USER_ROLES, roles);
                    }
                    if (expiresAt != null) {
                        httpHeaders.set(HEADER_TOKEN_EXPIRES_AT, expiresAt);
                    }
                    if (userId != null) {
                        httpHeaders.set(HEADER_USER_ID, userId);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private String extractExpiration(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Claims claims) {
            Date expiration = claims.getExpiration();
            if (expiration != null) {
                return Instant.ofEpochMilli(expiration.getTime()).toString();
            }
        }
        return null;
    }

    private String extractUserId(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Claims claims) {
            return claims.get("userId", String.class);
        }
        return null;
    }
}
