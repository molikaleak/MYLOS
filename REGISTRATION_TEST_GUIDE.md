# Registration Endpoint Testing Guide

## ✅ **Registration Implementation Complete**

I have successfully implemented a registration endpoint with role support for your Loan Origination System. Here's what was added:

## 🔗 **New Endpoint:**

**POST** `http://localhost:8080/api/auth/register`

## 📋 **Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "phone": "+85512345678",
  "password": "securePassword123",
  "branchId": 1,
  "roleCode": "LOAN_OFFICER"
}
```

### **Role Codes (from MUserRole table):**
- `ADMIN` - System administrator
- `LOAN_OFFICER` - Loan processing officer  
- `CUSTOMER_SERVICE` - Customer service representative
- `APPROVER` - Loan approval authority
- `AUDITOR` - System auditor

## 🧪 **Testing in Postman:**

### **Test 1: Successful Registration**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "test_user",
  "email": "test@example.com",
  "phone": "+85598765432",
  "password": "Test@123",
  "branchId": 1,
  "roleCode": "LOAN_OFFICER"
}
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 1800,
  "message": "Registration successful"
}
```

### **Test 2: Duplicate Username**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "test_user",  // Already exists
  "email": "new@example.com",
  "password": "Test@123",
  "roleCode": "LOAN_OFFICER"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Username already exists: test_user"
}
```

### **Test 3: Duplicate Email**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "new_user",
  "email": "test@example.com",  // Already exists
  "password": "Test@123",
  "roleCode": "LOAN_OFFICER"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Email already exists: test@example.com"
}
```

### **Test 4: Missing Required Fields**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "test_user"
  // Missing email, password, roleCode
}
```

**Expected Response:** Server error or validation error (depending on validation implementation)

## 🔄 **Complete Authentication Flow:**

1. **Register** → Get tokens immediately
2. **Use access token** on protected endpoints
3. **Refresh token** when access token expires
4. **Logout** → Token blacklisted in Redis
5. **Try to use blacklisted token** → Access denied

## 📊 **Database Changes:**

### **User Record (TUser table):**
- Password automatically encrypted with BCrypt
- Status set to "ACTIVE"
- Created timestamp set automatically
- Role code stored for authorization
- Refresh token stored for session management

### **Role Validation:**
The system stores the role code but doesn't validate against `MUserRole` table in the current implementation. You can enhance this by:
1. Adding foreign key constraint
2. Validating role exists before registration
3. Loading role permissions during authentication

## 🛡️ **Security Features:**

1. **Password Encryption:** BCrypt password encoding
2. **Duplicate Prevention:** Username and email uniqueness checks
3. **Immediate Authentication:** Returns JWT tokens after registration
4. **Role-Based Access:** Role code stored for future authorization
5. **Redis Integration:** Logout functionality with token blacklisting

## 🔧 **Implementation Details:**

### **Files Modified:**
1. `AuthenticationService.java` - Added `register()` method
2. `AuthenticationController.java` - Added `/api/auth/register` endpoint
3. `RegisterRequest.java` - Already existed with role support
4. `JwtAuthenticationFilter.java` - Already included register in public endpoints
5. `SecurityConfig.java` - Already allowed public access to `/api/auth/**`

### **Registration Process:**
1. Validate username/email uniqueness
2. Create user with encrypted password
3. Set default status "ACTIVE"
4. Generate JWT tokens
5. Store refresh token in database
6. Return tokens to client

## 🚀 **Quick Start Testing:**

```bash
# 1. Start services
docker-compose up -d

# 2. Start application
./mvnw spring-boot:run

# 3. Test registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "loan_officer_1",
    "email": "officer@bank.com",
    "phone": "+85512345678",
    "password": "SecurePass123",
    "branchId": 1,
    "roleCode": "LOAN_OFFICER"
  }'
```

## 📝 **Sample Test Data:**

```json
[
  {
    "username": "admin_user",
    "email": "admin@bank.com",
    "password": "Admin@123",
    "roleCode": "ADMIN",
    "branchId": 1
  },
  {
    "username": "customer_service",
    "email": "service@bank.com", 
    "password": "Service@123",
    "roleCode": "CUSTOMER_SERVICE",
    "branchId": 1
  },
  {
    "username": "approver_1",
    "email": "approver@bank.com",
    "password": "Approve@123",
    "roleCode": "APPROVER",
    "branchId": 1
  }
]
```

## ⚠️ **Important Notes:**

1. **Role Management:** Consider adding a role validation service to ensure only valid role codes are accepted
2. **Password Policy:** Add password strength validation (minimum length, special characters, etc.)
3. **Email Validation:** Add proper email format validation
4. **Phone Validation:** Add phone number format validation for Cambodia (+855 prefix)
5. **Branch Validation:** Validate branchId exists in MBranch table

## ✅ **Next Steps:**

1. Test the registration endpoint with various role codes
2. Implement role-based authorization for protected endpoints
3. Add email verification for registered users
4. Implement password reset functionality
5. Add user profile management endpoints

The registration endpoint is now fully functional and integrates with the existing authentication system, including Redis token blacklisting for logout functionality.