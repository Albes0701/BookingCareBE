# Account Service API Endpoints Documentation

**Generated**: November 26, 2025  
**Service**: Account Service  
**Base URL**: `http://localhost:8070/api/v1/account`

---

## 📊 Tổng hợp Endpoints

### **1. Authentication Endpoints** (`/api/v1/account/auth`)

#### 1.1 Login
- **Method**: `POST`
- **Endpoint**: `/api/v1/account/auth/login`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ❌ Không yêu cầu
- **Authorization**: None
- **File**: `AuthController.java` (Line 26-28)

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user-123",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }
}
```

**Error Response** (400/401):
```json
{
  "error": "Invalid email or password",
  "message": "Authentication failed"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8070/api/v1/account/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

---

#### 1.2 Register User
- **Method**: `POST`
- **Endpoint**: `/api/v1/account/auth/register`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ❌ Không yêu cầu
- **Authorization**: None
- **File**: `AuthController.java` (Line 31-40)

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "0123456789",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main Street"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user-456",
    "email": "newuser@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }
}
```

**Error Response** (400):
```json
{
  "error": "User already exists",
  "message": "Email already registered"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8070/api/v1/account/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "0123456789",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "123 Main Street"
  }'
```

---

#### 1.3 Register Doctor
- **Method**: `POST`
- **Endpoint**: `/api/v1/account/auth/register/doctor`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: `hasRole('ADMIN')`
- **File**: `AuthController.java` (Line 53-56)

**Request Header**:
```
Authorization: Bearer <admin_access_token>
```

**Request Body**:
```json
{
  "email": "doctor@clinic.com",
  "password": "doctorpass123",
  "fullName": "Dr. Jane Smith",
  "phoneNumber": "0987654321",
  "dateOfBirth": "1985-05-20",
  "gender": "FEMALE",
  "address": "456 Medical Center",
  "specialization": "Cardiology",
  "licenseNumber": "MED-2024-001",
  "yearsOfExperience": 10
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "doctor-789",
    "email": "doctor@clinic.com",
    "fullName": "Dr. Jane Smith",
    "role": "DOCTOR"
  }
}
```

**Error Response** (403):
```json
{
  "error": "Unauthorized",
  "message": "Only ADMIN can register doctors"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8070/api/v1/account/auth/register/doctor \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "email": "doctor@clinic.com",
    "password": "doctorpass123",
    "fullName": "Dr. Jane Smith",
    "phoneNumber": "0987654321",
    "dateOfBirth": "1985-05-20",
    "gender": "FEMALE",
    "address": "456 Medical Center",
    "specialization": "Cardiology",
    "licenseNumber": "MED-2024-001",
    "yearsOfExperience": 10
  }'
```

---

#### 1.4 Change Password
- **Method**: `POST`
- **Endpoint**: `/api/v1/account/auth/change-password`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None (Authenticated users only)
- **File**: `AuthController.java` (Line 42-52)

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Request Body**:
```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword456",
  "confirmPassword": "newpassword456"
}
```

**Response** (200 OK):
```json
{
  "message": "Password changed successfully"
}
```

**Error Response** (400):
```json
{
  "error": "Current password is incorrect",
  "message": "Password change failed"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8070/api/v1/account/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "currentPassword": "oldpassword123",
    "newPassword": "newpassword456",
    "confirmPassword": "newpassword456"
  }'
```

---

### **2. User Endpoints** (`/api/v1/account/users`)

#### 2.1 Get All Users
- **Method**: `GET`
- **Endpoint**: `/api/v1/account/users`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None
- **File**: `UserController.java` (Line 20-25)

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Request Body**: None

**Response** (200 OK):
```json
[
  {
    "id": "user-123",
    "email": "user1@example.com",
    "fullName": "John Doe",
    "phoneNumber": "0123456789",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "123 Main Street",
    "role": "USER",
    "createdDate": "2024-01-10T10:30:00Z",
    "isDeleted": false
  },
  {
    "id": "user-456",
    "email": "user2@example.com",
    "fullName": "Jane Smith",
    "phoneNumber": "0987654321",
    "dateOfBirth": "1992-05-20",
    "gender": "FEMALE",
    "address": "456 Oak Avenue",
    "role": "USER",
    "createdDate": "2024-01-11T14:45:00Z",
    "isDeleted": false
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8070/api/v1/account/users \
  -H "Authorization: Bearer <token>"
```

---

#### 2.2 Get User by ID
- **Method**: `GET`
- **Endpoint**: `/api/v1/account/users/{id}`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None
- **File**: `UserController.java` (Line 27-32)

**Path Parameters**:
```
id: user-123 (User ID)
```

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Response** (200 OK):
```json
{
  "id": "user-123",
  "email": "user1@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0123456789",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main Street",
  "role": "USER",
  "createdDate": "2024-01-10T10:30:00Z",
  "isDeleted": false
}
```

**Error Response** (404):
```json
{
  "error": "Not Found",
  "message": "User not found"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8070/api/v1/account/users/user-123 \
  -H "Authorization: Bearer <token>"
```

---

#### 2.3 Get User by Email
- **Method**: `GET`
- **Endpoint**: `/api/v1/account/users/email/{email}`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None
- **File**: `UserController.java` (Line 34-39)
- **⚠️ Note**: Endpoint này có thể gây xung đột path với `/users/{id}`

**Path Parameters**:
```
email: user@example.com (User email)
```

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Response** (200 OK):
```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0123456789",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main Street",
  "role": "USER",
  "createdDate": "2024-01-10T10:30:00Z",
  "isDeleted": false
}
```

**Error Response** (404):
```json
{
  "error": "Not Found",
  "message": "User not found"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8070/api/v1/account/users/email/user@example.com \
  -H "Authorization: Bearer <token>"
```

---

#### 2.4 Get Current User Profile
- **Method**: `GET`
- **Endpoint**: `/api/v1/account/users/me`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None (Authenticated users only)
- **File**: `UserController.java` (Line 52-56)

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Response** (200 OK):
```json
{
  "id": "user-123",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0123456789",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main Street",
  "role": "USER",
  "createdDate": "2024-01-10T10:30:00Z",
  "isDeleted": false
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8070/api/v1/account/users/me \
  -H "Authorization: Bearer <token>"
```

---

#### 2.5 Update User Profile
- **Method**: `PUT`
- **Endpoint**: `/api/v1/account/users/{id}`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None (User can update own profile)
- **File**: `UserController.java` (Line 41-48)

**Path Parameters**:
```
id: user-123 (User ID)
```

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Request Body**:
```json
{
  "fullName": "John Updated",
  "phoneNumber": "0912345678",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "789 New Street"
}
```

**Response** (200 OK):
```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "John Updated",
  "phoneNumber": "0912345678",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "789 New Street",
  "role": "USER",
  "createdDate": "2024-01-10T10:30:00Z",
  "updatedDate": "2024-01-15T09:20:00Z",
  "isDeleted": false
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8070/api/v1/account/users/user-123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "fullName": "John Updated",
    "phoneNumber": "0912345678",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "789 New Street"
  }'
```

---

#### 2.6 Delete User (Soft Delete)
- **Method**: `DELETE`
- **Endpoint**: `/api/v1/account/users/{id}`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None
- **File**: `UserController.java` (Line 50-57)

**Path Parameters**:
```
id: user-123 (User ID)
```

**Request Header**:
```
Authorization: Bearer <user_access_token>
```

**Request Body**: None

**Response** (200 OK):
```json
{
  "message": "User successfully deleted",
  "userId": "user-123",
  "status": 200
}
```

**Error Response** (404):
```json
{
  "error": "Not Found",
  "message": "User not found"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8070/api/v1/account/users/user-123 \
  -H "Authorization: Bearer <token>"
```

---

#### 2.7 Get Current User's Doctor Profile
- **Method**: `GET`
- **Endpoint**: `/api/v1/account/users/me/doctor`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: None (Any authenticated user)
- **File**: `UserController.java` (Line 59-64)
- **⚠️ Note**: Route này có thể gây xung đột với `GET /users/me` nếu không cẩn thận

**Request Header**:
```
Authorization: Bearer <doctor_access_token>
```

**Response** (200 OK):
```json
{
  "id": "doctor-789",
  "userId": "user-123",
  "specialization": "Cardiology",
  "licenseNumber": "MED-2024-001",
  "yearsOfExperience": 10,
  "bio": "Expert in heart diseases",
  "clinicId": "clinic-001",
  "isVerified": true,
  "createdDate": "2024-01-10T10:30:00Z"
}
```

**Error Response** (404):
```json
{
  "error": "Not Found",
  "message": "Doctor profile not found"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8070/api/v1/account/users/me/doctor \
  -H "Authorization: Bearer <token>"
```

---

### **3. Account Management Endpoints** (`/api/v1/account/accounts`)

#### 3.1 Get All Accounts
- **Method**: `GET`
- **Endpoint**: `/api/v1/account/accounts`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: `hasRole('ADMIN')`
- **File**: `AccountManagementController.java` (Line 19-23)

**Request Header**:
```
Authorization: Bearer <admin_access_token>
```

**Request Body**: None

**Response** (200 OK):
```json
[
  {
    "accountId": "account-001",
    "email": "user1@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "createdDate": "2024-01-10T10:30:00Z",
    "isDeleted": false,
    "lastLoginDate": "2024-01-15T14:25:00Z"
  },
  {
    "accountId": "account-002",
    "email": "doctor@clinic.com",
    "fullName": "Dr. Jane Smith",
    "role": "DOCTOR",
    "createdDate": "2024-01-12T08:15:00Z",
    "isDeleted": false,
    "lastLoginDate": "2024-01-15T09:50:00Z"
  },
  {
    "accountId": "account-003",
    "email": "admin@system.com",
    "fullName": "Admin User",
    "role": "ADMIN",
    "createdDate": "2024-01-01T00:00:00Z",
    "isDeleted": false,
    "lastLoginDate": "2024-01-15T16:00:00Z"
  }
]
```

**Error Response** (403):
```json
{
  "error": "Forbidden",
  "message": "Only ADMIN can access this resource"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8070/api/v1/account/accounts \
  -H "Authorization: Bearer <admin_token>"
```

---

#### 3.2 Soft Delete Account
- **Method**: `DELETE`
- **Endpoint**: `/api/v1/account/accounts/{accountId}`
- **Status**: ✅ **Đang sử dụng**
- **Authentication**: ✅ Yêu cầu
- **Authorization**: `hasRole('ADMIN')`
- **File**: `AccountManagementController.java` (Line 25-29)

**Path Parameters**:
```
accountId: account-001 (Account ID)
```

**Request Header**:
```
Authorization: Bearer <admin_access_token>
```

**Request Body**: None

**Response** (200 OK):
```json
{
  "message": "Account deleted successfully"
}
```

**Error Response** (404):
```json
{
  "error": "Not Found",
  "message": "Account not found"
}
```

**Error Response** (403):
```json
{
  "error": "Forbidden",
  "message": "Only ADMIN can delete accounts"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8070/api/v1/account/accounts/account-001 \
  -H "Authorization: Bearer <admin_token>"
```

---

## 📊 Summary Table

| # | Endpoint | Method | Status | Auth | Role | File | Mục đích |
|---|----------|--------|--------|------|------|------|---------|
| 1 | `/auth/login` | POST | ✅ | ❌ | - | AuthController.java:26 | Đăng nhập |
| 2 | `/auth/register` | POST | ✅ | ❌ | - | AuthController.java:31 | Đăng ký user |
| 3 | `/auth/register/doctor` | POST | ✅ | ✅ | ADMIN | AuthController.java:53 | Đăng ký bác sĩ |
| 4 | `/auth/change-password` | POST | ✅ | ✅ | - | AuthController.java:42 | Đổi mật khẩu |
| 5 | `/users` | GET | ✅ | ✅ | - | UserController.java:20 | Lấy all users |
| 6 | `/users/{id}` | GET | ✅ | ✅ | - | UserController.java:27 | Lấy user by ID |
| 7 | `/users/email/{email}` | GET | ✅ | ✅ | - | UserController.java:34 | Lấy user by email |
| 8 | `/users/{id}` | PUT | ✅ | ✅ | - | UserController.java:41 | Update profile |
| 9 | `/users/{id}` | DELETE | ✅ | ✅ | - | UserController.java:50 | Soft delete user |
| 10 | `/users/me` | GET | ✅ | ✅ | - | UserController.java:52 | Lấy current user |
| 11 | `/users/me/doctor` | GET | ✅ | ✅ | - | UserController.java:59 | Lấy doctor profile |
| 12 | `/accounts` | GET | ✅ | ✅ | ADMIN | AccountMgmt.java:19 | Lấy all accounts |
| 13 | `/accounts/{id}` | DELETE | ✅ | ✅ | ADMIN | AccountMgmt.java:25 | Soft delete account |

---

## 🔴 Issues & Concerns

### **Issue 1: Duplicate Delete Endpoints**
```
❌ /users/{id} DELETE (UserController)
❌ /accounts/{accountId} DELETE (AccountManagementController)

✅ Suggestion: Unify vào một endpoint hoặc làm rõ sự khác biệt
   - /users/{id}: Xóa user (soft delete, chính user)
   - /accounts/{id}: Xóa account (admin only)
```

### **Issue 2: Email Endpoint Route Conflict**
```
⚠️ GET /users/email/{email} 
   GET /users/{id}
   
Problem: 
- Spring Spring có thể nhầm lẫn "email" vs "{id}"
- "email" sẽ được coi là ID string
- Cần @GetMapping order đúng

✅ Solution: Đặt /email/{email} TRƯỚC /{id}
   hoặc dùng query parameter: GET /users?email=...&id=...
```

### **Issue 3: Doctor Profile Endpoint Route Conflict**
```
⚠️ GET /users/me/doctor
   GET /users/{id}
   
Problem:
- "me" sẽ được coi là {id}
- Cần ensure /me routes được defined TRƯỚC /{id}

✅ Solution: Đặt @GetMapping("/me") TRƯỚC @GetMapping("/{id}")
            Đặt @GetMapping("/me/doctor") TRƯỚC @GetMapping("/{id}")
```

### **Issue 4: Missing Error Handling in Register**
```
⚠️ AuthController.java line 35-40:
   catch (RuntimeException e) {
       return ResponseEntity.badRequest().build();
   }

Problem:
- Exception bị "nuốn" không log
- Không trả về chi tiết error

✅ Solution: 
   catch (RuntimeException e) {
       log.error("Registration failed", e);
       return ResponseEntity.badRequest().body(
           Map.of("error", e.getMessage())
       );
   }
```

### **Issue 5: Route Method Duplication**
```
⚠️ DELETE /users/{id}
   DELETE /accounts/{accountId}
   
Both are soft delete operations but at different levels:
- User delete: Từ UserController
- Account delete: Từ AccountManagementController

✅ Recommendation:
   Unified approach with clear semantics
```

---

## ✅ Recommendations

### **1. Fix Route Ordering** (High Priority)
```java
// UserController.java - correct order
@GetMapping("/me/doctor")  // ✅ Định nghĩa trước
@GetMapping("/me")         // ✅ Định nghĩa trước
@GetMapping("/email/{email}") // ✅ Định nghĩa trước
@GetMapping("/{id}")       // ✅ Định nghĩa sau cùng
```

### **2. Fix Email Parameter** (High Priority)
```java
// Thay thế:
@GetMapping("/email/{email}")

// Bằng:
@GetMapping
public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
    // ...
}
```

### **3. Improve Error Handling** (Medium Priority)
```java
@PostMapping("/register")
public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
    try {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        log.error("Registration failed", e);
        return ResponseEntity.status(400).body(
            Map.of(
                "error": "REGISTRATION_FAILED",
                "message": e.getMessage()
            )
        );
    }
}
```

### **4. Add Input Validation** (Medium Priority)
```java
@Valid @RequestBody UpdateProfileRequest request
// Ensure all DTOs have proper @NotNull, @Email annotations
```

### **5. Add Pagination & Filtering** (Low Priority)
```java
@GetMapping
public ResponseEntity<Page<UserDTO>> getAllUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String role
) {
    // ...
}
```

### **6. Add API Versioning** (Low Priority)
```java
@RequestMapping("/api/v2/account/users")
// Facilitate future API changes
```

### **7. Document with OpenAPI/Swagger** (Medium Priority)
```java
@Operation(summary = "Get all users", description = "Retrieve all active users")
@ApiResponse(responseCode = "200", description = "List of users")
@ApiResponse(responseCode = "401", description = "Unauthorized")
```

### **8. Add Request/Response Logging** (Medium Priority)
```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    log.info("Login attempt for email: {}", request.getEmail());
    AuthResponse response = authService.login(request);
    log.info("Login successful for user: {}", response.getUser().getId());
    return ResponseEntity.ok(response);
}
```

---

## 🔒 Security Considerations

1. ✅ Password hashing (đã implement)
2. ✅ JWT token (đã implement)
3. ✅ Role-based access control (đã implement)
4. ⚠️ Rate limiting (MISSING)
5. ⚠️ CORS configuration (MISSING)
6. ⚠️ Input validation (PARTIAL)
7. ⚠️ SQL injection prevention (qua ORM)
8. ⚠️ XSS protection (qua JSON API)

---

## 📈 Performance Considerations

1. ⚠️ Get all users - có thể slow với many users → Add pagination
2. ⚠️ No caching - nên cache user profiles
3. ⚠️ No database indexing notes - add indexing cho email, id
4. ✅ Connection pooling (qua Spring Boot)
5. ✅ Lazy loading (qua JPA)

---

## 🔄 API Evolution Roadmap

### **Phase 1: Fixes** (Immediate)
- [ ] Fix route ordering
- [ ] Replace email path variable with query param
- [ ] Improve error handling

### **Phase 2: Enhancements** (Next Sprint)
- [ ] Add pagination
- [ ] Add filtering
- [ ] Add request logging

### **Phase 3: Standards** (Later)
- [ ] Add Swagger/OpenAPI
- [ ] Add rate limiting
- [ ] Add CORS configuration
- [ ] Add caching layer

### **Phase 4: Advanced** (Future)
- [ ] Add 2FA
- [ ] Add OAuth2/SSO
- [ ] Add audit logging
- [ ] Add API versioning

---

**Document Version**: 1.0  
**Last Updated**: November 26, 2025  
**Reviewed By**: Development Team
