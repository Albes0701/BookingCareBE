# 📘 Project Technical Documentation

## 1. Tổng quan dự án
- **Mục tiêu**: Hệ thống BookingCare quản lý tài khoản người dùng, lịch khám, gói dịch vụ và các tương tác phụ trợ (thanh toán, thông báo) theo mô hình microservice.
- **Kiến trúc tổng thể**: Microservices Spring Boot. Mỗi dịch vụ độc lập deploy, giao tiếp qua API HTTP. Spring Cloud Gateway làm reverse proxy, Discovery Service (Eureka) quản lý service registry, Config Server phân phối cấu hình tập trung. Dữ liệu hiện tại chủ yếu nằm ở account-service (PostgreSQL + Flyway).
- **Ngôn ngữ & công nghệ chính**:
  - Backend: Java 21, Spring Boot 3.5.x, Spring Cloud 2025.0.x, Spring Security, Spring Data JPA, MapStruct, Lombok, Flyway, JJWT.
  - Hạ tầng: Spring Cloud Config, Eureka Discovery, Spring Cloud Gateway, Dockerfile cho từng dịch vụ.
  - Database: PostgreSQL (account-service); các service khác chưa khai báo DB.
  - Frontend: React (thư mục `frontend/`) – ngoài phạm vi tài liệu này.

## 2. Cấu trúc thư mục
```
BookingCare/
├── backend/
│   ├── docs/                           # Tài liệu kiến trúc & bảo mật
│   └── services/
│       ├── account/                    # Dịch vụ quản lý tài khoản & xác thực
│       ├── booking/                    # Dịch vụ đặt lịch (stub)
│       ├── clinic/                     # Dịch vụ phòng khám (stub)
│       ├── config-server/              # Spring Cloud Config Server
│       ├── discovery/                  # Eureka Server
│       ├── expertise/                  # Dịch vụ chuyên khoa (stub)
│       ├── gateway/                    # Spring Cloud Gateway
│       ├── notification/               # Dịch vụ thông báo (stub)
│       ├── package-service/            # Dịch vụ gói dịch vụ (stub)
│       ├── payment/                    # Dịch vụ thanh toán (stub)
│       └── schedule/                   # Dịch vụ lịch khám (stub)
└── frontend/                           # React client
```
- **account/**: Dịch vụ đầy đủ nhất. Cấu trúc chuẩn `controller` → `service` → `repository`/`entity` với DTO và mapper MapStruct, bảo mật JWT.
- **config-server/**: Khởi động Spring Cloud Config, cung cấp cấu hình YAML cho từng service (cổng, DB, eureka, route).
- **discovery/**: Eureka server cho service registry.
- **gateway/**: Spring Cloud Gateway, nhận request công khai và phân phối vào các service qua service discovery.
- Các service domain khác hiện giữ vai trò placeholder (chủ yếu skeleton `Application` + controller test).

## 3. Luồng xử lý chính
### 3.1 Luồng cấu hình & khởi động
1. Config Server (`config-server`) load YAML trong `src/main/resources/configurations/*.yml`.
2. Khi các service khởi động, chúng sử dụng `spring.config.import=optional:configserver:http://localhost:8888` để lấy cấu hình (cổng, datasource, eureka).
3. Mỗi service đăng ký vào Eureka (`discovery-service`) để gateway và các service khác định vị.

### 3.2 Luồng HTTP qua Gateway
1. Client gọi `gateway-service` (cổng 8222).
2. Gateway dựa trên `Path` predicate ánh xạ request đến service tương ứng (ví dụ `/api/v1/account/**` → `ACCOUNT-SERVICE`).
3. Gateway chuyển tiếp request bằng service discovery (URI `lb://...`). Chưa có JWT filter ở gateway; bảo mật được áp dụng tại account-service hiện tại.

### 3.3 Luồng đăng nhập (Account Service)
Input: `POST /api/v1/account/auth/login` với `{ username, password }`.
1. `AuthController.login` nhận request → gọi `AuthService.login`.
2. `AuthService` truy vấn `AccountsRepo` để tìm tài khoản, kiểm tra trạng thái `isDeleted`, xác thực mật khẩu (BCrypt).
3. Sau khi xác thực, `JwtService.generateAccessToken` và `generateRefreshToken` tạo JWT (claim authorities lấy từ `Accounts.roles`).
4. `UsersMapper` map `Users` entity sang `UserDTO`.
5. Output: `AuthResponse` chứa `accessToken`, `refreshToken`, `tokenType=Bearer`, `user`.

### 3.4 Luồng truy vấn & cập nhật hồ sơ
- `GET /api/v1/account/users` → `UserService.getAllUsers()` → `UsersRepo.findAllActiveUsers()` → map sang DTO.
- `PUT /api/v1/account/users/{id}`:
  1. Validate đầu vào bằng Bean Validation (record `UpdateProfileRequest`).
  2. `UsersRepo.findByIdAndNotDeleted` & kiểm tra uniqueness email/phone.
  3. `UsersMapper.updateUsersFromRequest` cập nhật entity (MapStruct + `@MappingTarget`).
  4. Lưu thông qua `UsersRepo.save`.

### 3.5 Luồng bảo mật JWT nội bộ
1. `SecurityConfig` đăng ký filter chain không trạng thái, disable CSRF/basic/form login.
2. `JWTTokenValidatorFilter` chạy trước `BasicAuthenticationFilter` để giải mã JWT từ header, tạo `UsernamePasswordAuthenticationToken` và đặt vào `SecurityContext`.
3. Sau khi xác thực, `JWTTokenGeneratorFilter` (chỉ cho `/auth/login`) có thể phát hành token mới vào response.

## 4. Chi tiết các thành phần
### 4.1 Config Server (`backend/services/config-server`)
- **Chức năng**: Cung cấp cấu hình tập trung.
- **Class chính**:
  - `ConfigServerApplication` (mặc định Spring Boot, không tuỳ chỉnh thêm trong repo).
- **Resources**:
  - `configurations/*.yml`: cấu hình theo service.
    - `account-service.yml`: cổng 8070, cấu hình PostgreSQL, Flyway, Eureka.
    - `gateway-service.yml`: cổng 8222, khai báo route Gateway, bật discovery locator.
    - Các file khác đặt cổng mặc định và registry cho service stub.
- **Dependencies**: Spring Cloud Config Server, Spring Boot Actuator (theo pom).
- **Liên kết**: Mọi service backend cần `spring.config.import` trỏ đến config-server để nhận cấu hình nhất quán.

### 4.2 Discovery Service (`backend/services/discovery`)
- **Chức năng**: Eureka server.
- **Class chính**:
  - `DiscoveryApplication` (`@EnableEurekaServer`).
- **Dependencies**: Spring Cloud Netflix Eureka Server.
- **Liên kết**: Gateway và các microservice đăng ký vào đây, cho phép load balancing (`lb://SERVICE`).

### 4.3 Gateway Service (`backend/services/gateway`)
- **Chức năng**: Reverse proxy, route đến các microservice dựa trên path.
- **Config**: `config-server` cung cấp danh sách route; bật discovery locator nên có thể truy cập service theo `SERVICE-ID`.
- **Hiện trạng**: Chưa có filter bảo mật; routing logic đơn giản.
- **Liên kết**: Điểm vào duy nhất cho client; giao tiếp với account-service và dịch vụ khác qua HTTP nội bộ.

### 4.4 Account Service (`backend/services/account`)
- **Chức năng chính**: 
  - Quản lý tài khoản (`Accounts`, `Users`, `Roles`).
  - Cung cấp API xác thực (login, register, change password).
  - Quản lý hồ sơ người dùng (CRUD logic mềm xóa/phục hồi).
  - Phát hành & xác thực JWT.
- **Packages chính**:
  - `controller`: `AuthController`, `UserController`.
  - `service`: `AuthService`, `UserService`, `JwtService`.
  - `security`: cấu hình `SecurityConfig`, filter `JWTTokenValidatorFilter`, `JWTTokenGeneratorFilter`, `RequestValidationBeforeFilter`.
  - `repository`: `AccountsRepo`, `UsersRepo`, `RolesRepo`, `RefreshTokenRepo`.
  - `entity`: `Accounts`, `Users`, `Roles`, `Gender`, `RefreshToken`.
  - `dto`: Records request/response (`AuthRequest`, `AuthResponse`, `RegisterRequest`, `ChangePasswordRequest`, `UpdateProfileRequest`, `UserDTO`).
  - `mapper`: MapStruct interface (`UsersMapper`, `AccountsMapper`); implementation tự sinh tại `target/generated-sources`.
- **Luồng nội bộ**:
  - Controller → Service (logic nghiệp vụ) → Repository (Spring Data) → Entity/DTO mapping (MapStruct).
  - Validation: Bean Validation annotations trên DTO record.
  - Security: Stateless, JWT filter chain, PasswordEncoder (BCrypt).
- **External dependencies**: PostgreSQL (JPA), Flyway migrations, Spring Security, MapStruct, JJWT.
- **Liên kết**:
  - `AuthService` dùng `JwtService` + `UsersMapper`.
  - Filter sử dụng `SecurityConstants.JWT_HEADER`.
  - Config server cấp DSN Postgres + secret JWT qua `application.yml`.

### 4.5 Các microservice domain khác (booking, clinic, expertise, notification, package-service, payment, schedule)
- **Trạng thái**: Skeleton.
- **Cấu trúc**: Mỗi service có `Application` class và trong một số trường hợp controller test (`/api/v1/.../test`).
- **Mục đích**: placeholder cho phát triển tương lai. Cần bổ sung entity/service/repository tương tự account khi triển khai thực.
- **Liên kết**: Đã khai báo route tại gateway và cấu hình service ở config-server.

## 5. Design patterns & Coding conventions
- **Layered Architecture**: Controller → Service → Repository → Entity. DTO/Mapper giảm coupling.
- **Repository Pattern**: Spring Data JPA interface mở rộng `JpaRepository`, custom query qua `@Query`.
- **DTO + MapStruct**: Tách API contract khỏi entity, map hai chiều, dùng `@MappingTarget` để update.
- **Security Filters Chain**: Custom `OncePerRequestFilter` để validate/generate JWT.
- **Exception Handling**: Hiện chủ yếu ném `RuntimeException`; cần bổ sung `@ControllerAdvice` cho consistency (lưu ý trong extension).
- **Coding conventions**:
  - Java packages theo `com.bookingcare.<service>.<layer>`.
  - REST path dạng `/api/v1/<service>/...`.
  - Dùng Lombok (`@Slf4j`, `@RequiredArgsConstructor`, `@Builder`) để giảm boilerplate.
  - Bean Validation annotations trên record DTO.
  - Configuration qua YAML, tuân theo Spring Boot conventions.

## 6. Extension guideline
### 6.1 Tạo microservice mới
1. **Bootstrap**: Copy mẫu `spring-boot` trong `backend/services/<service-template>` hoặc dùng Spring Initializr (Java 21, Spring Boot 3.5.x).
2. **Config Server**:
   - Tạo file `backend/services/config-server/src/main/resources/configurations/<service-name>.yml`.
   - Định nghĩa `server.port`, datasource (nếu có), `eureka.client.service-url`.
3. **Application YAML**: Trong service mới, giữ `spring.config.import` trỏ tới config server.
4. **Eureka & Gateway**:
   - Đảm bảo `spring.application.name` khớp với ID sử dụng trong gateway route.
   - Cập nhật `gateway-service.yml` (nếu cần route mới).
5. **Security**:
   - Nếu service yêu cầu bảo vệ tài nguyên, thêm dependency `spring-boot-starter-security`, tái sử dụng `JwtService` (extract thành module chung hoặc duplicate tạm thời).
   - Thêm `JWTTokenValidatorFilter` trong `SecurityConfig`.
6. **Domain logic**:
   - Thiết kế entity (JPA) + repository interface.
   - Tạo DTO + mapper MapStruct (định nghĩa `@Mapper(componentModel = "spring")`).
   - Implement service layer với `@Transactional` và logging.
7. **Testing**:
   - Viết integration test cho controller/service bằng MockMvc hoặc WebTestClient.
8. **Docker**:
   - Điều chỉnh Dockerfile nếu cần môi trường runtime riêng.

### 6.2 Mở rộng account-service
1. **Thêm API**: Tạo controller mới hoặc mở rộng controller hiện có; tuân theo `/api/v1/account/...`.
2. **Bảo mật**: Cập nhật `SecurityConfig` để định nghĩa rule `authorizeHttpRequests`.
3. **Mapper**: Cập nhật interface MapStruct và chạy `mvn clean compile` để sinh code.
4. **Database**: Viết Flyway migration (`src/main/resources/db/migration/Vxxx__description.sql`).
5. **Config**: Thêm thuộc tính vào `account-service.yml` trong config server nếu cần (ví dụ secret mới).

## 7. API / Service Contract
### 7.1 Account Service Endpoints
| Endpoint | Method | Request Body | Response | Ghi chú |
|----------|--------|--------------|----------|---------|
| `/api/v1/account/auth/login` | POST | `{ "username": "...", "password": "..." }` | `AuthResponse` | Trả về access & refresh token |
| `/api/v1/account/auth/register` | POST | `RegisterRequest` (username, password, confirmPassword, fullName) | `AuthResponse` hoặc HTTP 400 | Tạo user + account, gán role mặc định `PATIENT` |
| `/api/v1/account/auth/change-password` | POST | `ChangePasswordRequest` | `{ "message": "Password changed successfully" }` | Yêu cầu JWT hợp lệ |
| `/api/v1/account/users` | GET | – | `List<UserDTO>` | Lấy danh sách user active |
| `/api/v1/account/users/{id}` | GET | – | `UserDTO` | Lấy user theo ID |
| `/api/v1/account/users/email/{email}` | GET | – | `UserDTO` | Lấy user theo email |
| `/api/v1/account/users/{id}` | PUT | `UpdateProfileRequest` | `UserDTO` | Cập nhật thông tin user, validate unique email/phone |
| `/api/v1/account/users/{id}` | DELETE | – | `{ "message": "...", "userId": id, "status": 200 }` | Soft delete |
| `/api/v1/account/users/{id}/restore` | POST | – | `UserDTO` | Khôi phục user đã soft delete |
| `/api/v1/account/users/test` | GET | – | `{ "message": "User Service Test", "status": 200 }` | Endpoint kiểm thử |

### 7.2 DTO Schema
- **AuthResponse**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "user": {
    "id": "string",
    "fullName": "string",
    "dateOfBirth": "2024-01-01",
    "email": "string",
    "phoneNumber": "string",
    "address": "string",
    "gender": "MALE|FEMALE|OTHER",
    "imageUrl": "string",
    "isDeleted": false
  }
}
```
- **UpdateProfileRequest**
```json
{
  "fullname": "string",
  "email": "user@example.com",
  "phone": "0987654321",
  "address": "string",
  "gender": "MALE",
  "image": "https://...",
  "birthdate": "1990-12-31"
}
```
- Validation error response hiện chưa chuẩn hóa (nên bổ sung `@ControllerAdvice`).

### 7.3 Testing
- **Unit/Integration**: Chưa có suite test. Khi bổ sung:
  - Dùng `@WebMvcTest` cho controller (mock service).
  - Dùng `@SpringBootTest` + Testcontainers PostgreSQL cho integration.
  - Kiểm thử bảo mật: Đảm bảo endpoint yêu cầu JWT trả 401 khi thiếu token.

## 8. Example: Implement module tương tự
Giả sử cần thêm module `analytics-service` để phân tích người dùng.

1. **Khởi tạo service**
   ```bash
   cp -r backend/services/booking backend/services/analytics
   ```
   Cập nhật `spring.application.name=analytics-service`.

2. **Khai báo cấu hình**
   - Tạo `backend/services/config-server/src/main/resources/configurations/analytics-service.yml`:
     ```yaml
     server:
       port: 8095
     spring:
       datasource: ...
       jpa:
         hibernate:
           ddl-auto: validate
     eureka:
       client:
         service-url:
           defaultZone: http://localhost:8761/eureka
     ```
   - Thêm route vào `gateway-service.yml`:
     ```yaml
     - id: analytics-service
       uri: lb:http://ANALYTICS-SERVICE
       predicates:
         - Path=/api/v1/analytics/**
     ```

3. **Thiết kế lớp logic**
   ```java
   @RestController
   @RequestMapping("/api/v1/analytics")
   @RequiredArgsConstructor
   public class AnalyticsController {
       private final AnalyticsService analyticsService;

       @GetMapping("/users/{id}")
       public ResponseEntity<AnalyticsDTO> analyze(@PathVariable String id) {
           return ResponseEntity.ok(analyticsService.analyzeUser(id));
       }
   }
   ```

   ```java
   @Service
   @RequiredArgsConstructor
   public class AnalyticsService {
       private final AnalyticsRepository repository;
       private final AnalyticsMapper mapper;

       @Transactional(readOnly = true)
       public AnalyticsDTO analyzeUser(String userId) {
           return repository.findByUserId(userId)
                   .map(mapper::toDto)
                   .orElseThrow(() -> new AnalyticsNotFoundException(userId));
       }
   }
   ```

   ```java
   @Mapper(componentModel = "spring")
   public interface AnalyticsMapper {
       AnalyticsDTO toDto(Analytics analytics);
   }
   ```

4. **Bảo mật**
   - Thêm dependency Spring Security.
   - Sao chép `SecurityConfig` dạng resource-server: include `JWTTokenValidatorFilter` tái sử dụng `JwtService`.
   - Xác định role yêu cầu: ví dụ `@PreAuthorize("hasRole('ADMIN')")` trên service.

5. **Kiểm thử**
   - Viết test MockMvc cho controller (mock service).
   - Integration test với token hợp lệ & không hợp lệ.

6. **Triển khai**
   - Build `mvn clean package`.
   - Dockerize sử dụng Dockerfile mẫu trong repo (cập nhật tên jar).

> Tuân thủ các convention hiện có (logging qua `@Slf4j`, DTO record, MapStruct) giúp module mới dễ dàng hòa nhập với hệ thống hiện hành.
