package com.bookingcare.expertise.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.bookingcare.expertise.security.filter.GatewayAuthenticationFilter;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public GatewayAuthenticationFilter gatewayAuthenticationFilter() {
        return new GatewayAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   GatewayAuthenticationFilter gatewayAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(gatewayAuthenticationFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/expertise/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/expertise/internal/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/expertise/doctors/by-user/{userId}").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/expertise/doctors/by-user/{userId}/profile").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/expertise").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/expertise/*").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
