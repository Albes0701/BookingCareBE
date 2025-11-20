# Security Setup Guide (Account Service)

## 1. Purpose
This document explains how the account-service implements JWT-based security and how to reuse the same approach across the BookingCare microservices. It is meant for backend developers who need to understand, maintain, or extend the current security design.

## 2. Architecture Overview
- **Stateless REST APIs**: Sessions are disabled. Every request must carry a valid JWT access token.
- **Spring Security filter chain**: Custom filters wrap the authentication process to validate inbound tokens and mint new ones after successful login.
- **JWT signing**: Tokens are signed with a symmetric `HS256` key stored in application configuration.
- **Role handling**: Roles are stored on the `Accounts` entity and embedded into the token as the `authorities` claim.

## 3. Key Components (Account Service)
- `SecurityConfig` (`backend/services/account/src/main/java/com/bookingcare/account/security/config/SecurityConfig.java`)
  - Disables CSRF, form login, and HTTP Basic.
  - Forces `SessionCreationPolicy.STATELESS`.
  - Registers custom filters in the correct order.
  - Declares URL authorization rules (auth endpoints open, everything else requires authentication).
  - Provides a project-wide `PasswordEncoder` and a per-request CORS configuration.
- `JwtService` (`backend/services/account/src/main/java/com/bookingcare/account/service/JwtService.java`)
  - Builds access and refresh tokens with configurable expiration times.
  - Exposes helpers to parse and validate tokens (`extractAllClaims`, `isTokenExpired`).
  - Uses the secret configured under the `jwt.*` properties.
- `JWTTokenValidatorFilter` (`backend/services/account/src/main/java/com/bookingcare/account/security/filter/JWTTokenValidatorFilter.java`)
  - Runs on every request except `/api/v1/account/auth/**`.
  - Reads the `Authorization` header, verifies the token via `JwtService`, and populates `SecurityContextHolder`.
- `JWTTokenGeneratorFilter` (`backend/services/account/src/main/java/com/bookingcare/account/security/filter/JWTTokenGeneratorFilter.java`)
  - Runs only on `/api/v1/account/auth/login`.
  - After a successful authentication it issues a fresh access token and attaches it to the response header.
- `RequestValidationBeforeFilter` (`backend/services/account/src/main/java/com/bookingcare/account/security/filter/RequestValidationBeforeFilter.java`)
  - Guard rail that rejects suspicious Basic-auth headers before they hit the authentication manager.
- `SecurityConstants` (`backend/services/account/src/main/java/com/bookingcare/account/constants/SecurityConstants.java`)
  - Central place for header names and legacy secrets used by older components.

## 4. Configuration
- `application.yml` (`backend/services/account/src/main/resources/application.yml`)
  - `jwt.secret`: long random string used as HS256 signing key.
  - `jwt.expiration`: access-token lifetime in milliseconds.
  - `jwt.refresh-token.expiration`: refresh-token lifetime in milliseconds.
  - Override these values per environment using Spring Config Server or environment variables (e.g. `JWT_SECRET`).
- `pom.xml` (`backend/services/account/pom.xml`)
  - Ensure `spring-boot-starter-security` and the JJWT dependencies (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) are present.
  - If you enable resource servers in other services, add `spring-boot-starter-oauth2-resource-server`.

## 5. Authentication Flow
1. **Login request**: `AuthService.login` authenticates user credentials against `AccountsRepo` and checks soft-delete flags on both account and user entities.
2. **Token issue**: `JwtService.generateAccessToken` and `generateRefreshToken` produce the tokens using the account’s username and role.
3. **Response**: `AuthResponse` wraps the tokens plus the mapped `UserDTO`.
4. **Subsequent calls**: Clients place the access token in the `Authorization: Bearer <token>` header. `JWTTokenValidatorFilter` verifies it and the request is permitted if the role matches the configured rules.
5. **Token renewal**: The refresh token is intended for future refresh endpoints (not yet in this module). Its lifetime is longer than the access token.

## 6. Applying Security to Other Services
Follow these steps when adding security to another microservice (e.g. booking-service, doctor-service):

1. **Add dependencies**: import `spring-boot-starter-security` and `jjwt-*` modules into the service’s `pom.xml`.
2. **Share configuration**: copy `jwt.*` properties into the service’s config or point the service to the centralized config server so all services share the same signing key and expirations.
3. **Reuse JwtService**: extract `JwtService` into a shared library module or duplicate it with a clear TODO to move to a common package. All services must use the same signing logic.
4. **Register validator filter**: for resource services (non-authentication), only the validator is required. Create a `SecurityConfig` that:
   - Sets the app to stateless.
   - Adds `JWTTokenValidatorFilter` before `BasicAuthenticationFilter`.
   - Defines authorization rules per endpoint (`permitAll` vs `hasRole`, etc.).
   - Optionally adds method-level security (`@EnableMethodSecurity`) for fine-grained access control.
5. **Skip generator filter**: only the account-service (or any service that authenticates credentials) needs the generator. Other services should not mint new tokens.
6. **Expose principal data**: downstream services can read claims from `SecurityContextHolder.getContext().getAuthentication()`. If they need extra information (user ID, tenant), put that into the JWT as additional claims.
7. **Testing**: write integration tests that call secured endpoints with and without tokens. You can reuse the `JwtService` utility to mint test tokens.

## 7. Gateway Integration (Recommended)
Implement JWT-aware security at the Spring Cloud Gateway layer so every request is validated before it hits downstream services:

1. **Dependencies**: add `spring-cloud-starter-gateway`, `spring-boot-starter-security`, and `jjwt-*` to the gateway project.
2. **Global filter**: implement a `GlobalFilter` or `WebFilter` that:
   - Extracts the Bearer token.
   - Uses the shared `JwtService` (or a gateway-specific helper) to validate the token.
   - Stores the result in the reactive `SecurityContext`.
3. **Security config**: define `SecurityWebFilterChain` that requires authentication for all routes except `/api/v1/account/auth/**`. Map route-specific role requirements as needed using `hasRole`.
4. **Propagate claims**: if downstream services should avoid re-validating tokens, forward trusted headers (e.g. `X-User-Id`, `X-Roles`) after successful validation. Otherwise, let the services re-run the validator filter for defense in depth.
5. **CORS**: configure allowed origins/methods at the gateway to centralize cross-origin rules.

## 8. Local Development & Testing
- **Manual testing**:
  1. Start account-service and its dependencies (DB, config server if used).
  2. `POST /api/v1/account/auth/login` with valid credentials.
  3. Use the returned access token on a protected endpoint (e.g. `GET /api/v1/account/users/me`) and confirm HTTP 200.
  4. Repeat with an invalid or expired token to confirm HTTP 401.
- **Automated tests**:
  - Add Spring MVC tests that mock valid tokens by stubbing `JwtService`.
  - For integration tests, use `TestRestTemplate` or `WebTestClient` with a pre-built token from `JwtService`.

## 9. Troubleshooting
- **401 Unauthorized**: confirm the token header is present and starts with `Bearer`. Check the service logs (they run at `DEBUG` level for Spring Security by default).
- **Token rejection after secret change**: all services must share the same `jwt.secret`. Rotate keys carefully and update every service together.
- **Clock skew issues**: ensure server clocks are synchronized (use NTP) when running on multiple hosts.
- **CORS failures**: adjust `corsConfigurationSource` to include the calling domain/port.

## 10. Next Steps
- Extract the shared security code (filters, `JwtService`, DTOs) into a dedicated `security-common` module to avoid duplication.
- Implement refresh-token endpoints to exchange the refresh token for a new access token.
- Tighten role checks on protected routes and add method-level annotations where business rules require them.
