# BookingCare Security Architecture

## 1. Scope & Objectives
This document describes the end-to-end security design of the BookingCare backend microservices. It supplements implementation-specific guides by focusing on architectural responsibilities, token lifecycle, configuration, and recommended hardening steps so that future changes remain consistent with the existing security posture.

## 2. Security Layers
- **Perimeter**  
  The Spring Cloud Gateway (`gateway-service`) is the public entry point. All client traffic should pass through it, where request routing, CORS policies, and (future) JWT validation can be enforced before reaching downstream services.

- **Service Mesh**  
  Microservices register with Eureka (`discovery-service`) and communicate via logical service IDs. Traffic between services is assumed to flow over an internal, trusted network segment; nonetheless, JWT validation is still performed inside the account-service and should be enabled in other services for defense in depth.

- **Configuration Management**  
  Secrets and environment-specific settings are delivered by Spring Cloud Config Server (`config-server`). Runtime secrets (e.g., `jwt.secret`) must be provided via secure configuration sources or environment variables in production.

- **Application Layer**  
  Each service may apply its own Spring Security configuration. Today, only `account-service` implements full JWT issuance and validation. Other services expose unauthenticated test endpoints and require additional security work before production use.

## 3. Identity & Access Management
- **Principal Source**: `Accounts` entity holds login credentials, soft-delete flag, and relation to `Roles`. Each account references exactly one `Users` profile.
- **Credential Storage**: Passwords are hashed with BCrypt (`PasswordEncoder` bean in `SecurityConfig`).
- **Role Model**: Simple role-based access control (RBAC). Roles are persisted (entity `Roles`) and serialized into JWT claims as `ROLE_<name>`. Authorities are parsed back into Spring Security `GrantedAuthority` objects.

## 4. Token Strategy
- **Token Types**: `JwtService` issues both access and refresh tokens (HS256 symmetric signing). Access tokens contain:
  - `sub`: username
  - `authorities`: comma-separated roles (prefixed by `ROLE_`)
  - `iat`, `exp`: issued-at and expiration timestamps (configurable via `jwt.expiration`)
- **Key Management**: Signing key derived from `jwt.secret`. Must be a long, random string; rotate periodically and keep out of source control.
- **Expiration Policy**:
  - Access token: configurable in milliseconds (default 24h).
  - Refresh token: default 7 days.
  - No refresh endpoint yet; tokens are minted during login, so refresh functionality needs implementation before exposing to clients.
- **Generation Flow**:
  1. Credentials validated by `AuthService`.
  2. `JwtService.generateAccessToken` & `generateRefreshToken` produce signed tokens.
  3. Tokens returned via `AuthResponse` and appended to response headers by `JWTTokenGeneratorFilter` on successful login.
- **Validation Flow**:
  1. `JWTTokenValidatorFilter` intercepts requests (except `/api/v1/account/auth/**`).
  2. Token extracted from `Authorization` header (`Bearer ...`).
  3. Claims parsed via `JwtService.extractAllClaims`.
  4. Authorities converted to `GrantedAuthority` list and stored in `SecurityContext`.
  5. Downstream controllers/services can rely on `SecurityContextHolder`.

## 5. Spring Security Filter Chain (Account Service)
Filter order (as declared in `SecurityConfig`):
1. `RequestValidationBeforeFilter` – Guards against malformed Basic auth headers to mitigate legacy credential abuse during migration.
2. `JWTTokenValidatorFilter` – Validates bearer tokens before authentication occurs.
3. `AuthoritiesLoggingAtFilter` / `AuthoritiesLoggingAfterFilter` – Provide audit logging around authentication events.
4. `JWTTokenGeneratorFilter` – Issues fresh access token post-authentication on `/auth/login`.

The chain runs under stateless session policy (`SessionCreationPolicy.STATELESS`), CSRF/form login/basic auth disabled. CORS configuration allows localhost ports for development and exposes `Authorization` header.

## 6. Configuration & Secrets
- **Properties Source**: `application.yml` inside each service delegates to Config Server via `spring.config.import`. The Config Server YAML files define ports, datasource credentials, and service discovery endpoints.
- **Secret Injection**: For production, override sensitive defaults (`jwt.secret`, database passwords) using environment variables or a secure vault connected to Config Server. Ensure that no secrets remain hard-coded in Git.
- **Rotation Process**:
  1. Set new secret under a feature flag or environment variable.
  2. Redeploy services with the new value while allowing old tokens to expire.
  3. Optionally support multiple keys by extending `JwtService` to handle key IDs (future enhancement).

## 7. Role-Based Authorization
- Current rules (`SecurityConfig`):
  - `/api/v1/account/auth/**`: `permitAll`.
  - `/api/v1/account/users/**`: `authenticated`.
  - Default: `authenticated`.
- No fine-grained `@PreAuthorize` annotations yet. Future work should:
  - Map roles (e.g., `ADMIN`, `STAFF`, `PATIENT`) to endpoint-level permissions.
  - Apply method-level security (`@EnableMethodSecurity` + annotations) for service/business-layer authorization.

## 8. Service-to-Service Communication
- Services should forward JWT tokens when calling other services to preserve end-user context. Until a gateway-level validator is in place, downstream services must validate tokens individually using a shared `JwtService` implementation or library module.
- Trusted system-to-system calls (without end-user context) should use separate credentials or client credentials grant once OAuth2 support is introduced.

## 9. Hardening & Best Practices
- **Input Validation**: Continue leveraging Bean Validation on DTOs. Add sanitization for fields stored in HTML or rendered to UI.
- **Error Handling**: Replace generic `RuntimeException` with custom exceptions and a `@ControllerAdvice` to avoid leaking stack traces while returning meaningful error codes.
- **Logging**: Keep security logs (`AuthoritiesLogging*` filters). Ensure tokens are never logged in plaintext; log only token metadata (subject, expiration).
- **Transport Security**: Enforce HTTPS at gateway level. For local dev, self-signed certificates or reverse proxy (nginx) can terminate TLS.
- **CORS**: Restrict allowed origins in production to trusted domains instead of wildcard localhost patterns.
- **Dependency Management**: Maintain latest Spring Boot/Spring Security/JJWT versions via `pom.xml`. Monitor CVEs regularly.
- **Database Security**: Use least-privilege DB accounts; avoid superuser credentials in production.
- **Testing**: Add penetration tests for authentication endpoints, automated security tests (e.g., invalid token, expired token, tampered signature).

## 10. Future Enhancements
1. **Gateway-Level JWT Validation**  
   Implement a `SecurityWebFilterChain` in `gateway-service` with a reactive JWT filter, ensuring that invalid tokens never reach downstream services.

2. **Refresh Token Endpoint**  
   Provide `/auth/refresh` to exchange valid refresh tokens for new access tokens. Store refresh tokens with revocation capability (`RefreshToken` entity present but not yet used).

3. **Multi-Role & Permission Matrix**  
   Define standardized role names and map them to permissions. Consider adding attribute-based access control for complex scenarios.

4. **Centralized Security Module**  
   Extract common security utilities (filters, DTOs, `JwtService`) into a shared library to avoid duplication across services.

5. **Audit & Monitoring**  
   Integrate with centralized logging/monitoring (ELK/Prometheus) to track authentication attempts, anomalies, and token misuse.

By following the guidelines above, new features or services can align with existing JWT-based security while progressively strengthening the system against emerging threats.
