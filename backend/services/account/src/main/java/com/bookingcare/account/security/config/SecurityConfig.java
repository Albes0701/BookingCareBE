package com.bookingcare.account.security.config;

import com.bookingcare.account.security.filter.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.lang.NonNull;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          JWTTokenValidatorFilter jwtTokenValidatorFilter
                                                           ) throws Exception {
        http
            // Stateless session management - no sessions for REST API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS configuration for microservice communication
            // .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))

            // Disable CSRF for stateless REST API
            .csrf(AbstractHttpConfigurer::disable)

            // Filter chain order for JWT-based authentication
            // 1. Request validation before any authentication
            .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)

            // 2. JWT token validation (must run before BasicAuthenticationFilter)
            .addFilterBefore(jwtTokenValidatorFilter, BasicAuthenticationFilter.class)

            // 3. Logging filters around authentication
            .addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class)
            .addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
            
            // Authorization rules for REST API endpoints
            .authorizeHttpRequests(requests -> requests
                // Public endpoints - authentication/authorization APIs
                .requestMatchers("/api/v1/account/auth/login",
                    "/api/v1/account/users/me",
                    "/api/v1/account/auth/register").permitAll()

                // bác sĩ xem thông tin của chính mình
                .requestMatchers("/api/v1/account/users/me/doctor").hasRole("DOCTOR")

                // các endpoint quản trị user
                .requestMatchers("/api/v1/account/users",
                                "/api/v1/account/auth/register/doctor",
                                "/api/v1/account/users/email/{email}")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/v1/account/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/account/users/{id}").hasRole("ADMIN")
                .requestMatchers("/api/v1/account/users/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/account/accounts/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            // Disable form login - not needed for REST API
            .formLogin(AbstractHttpConfigurer::disable)

            // Disable HTTP Basic - using JWT Bearer tokens only
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(@NonNull HttpServletRequest request) {
                CorsConfiguration config = new CorsConfiguration();

                // Allow requests from frontend and other microservices
                config.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));

                // REST API methods
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                // Allow credentials for JWT tokens
                config.setAllowCredentials(true);

                // Allow all headers including Authorization
                config.setAllowedHeaders(Arrays.asList("*"));

                // Expose Authorization header for JWT tokens
                config.setExposedHeaders(Arrays.asList("Authorization"));

                // Cache preflight for 1 hour
                config.setMaxAge(3600L);

                return config;
            }
        };
    }

    @Bean
    public JWTTokenValidatorFilter jwtTokenValidatorFilter() {
        return new JWTTokenValidatorFilter();
    }

  
}
