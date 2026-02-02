# Postman Testing Guide for Redis JWT Token Blacklisting

This guide provides step-by-step instructions to test the Redis-based JWT token blacklisting functionality in your Loan Origination System.

## Prerequisites

1. **Start Services:**
   ```bash
   docker-compose up -d
   ```
   This starts PostgreSQL (port 5432) and Redis (port 6379)

2. **Start Spring Boot Application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   Application runs on `http://localhost:8080`

3. **Postman Collection:** Import the following endpoints

## Authentication Endpoints

### 1. **Health Check**
**GET** `http://localhost:8080/api/auth/health`

**Response:**
```json
"Authentication service is running"
```

### 2. **User Login**
**POST** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "usernameOrEmail": "admin",
  "password": "password123"
}
```

**Successful Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 1800,
  "message": "Login successful"
}
```

**Save these tokens for subsequent tests!**

### 3. **Token Refresh**
**POST** `http://localhost:8080/api/auth/refresh`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "refreshToken": "your_refresh_token_here"
}
```

**Response:** Returns new access and refresh tokens.

## Testing Redis Token Blacklisting

### 4. **Protected Endpoint Test (Before Logout)**
First, test that your access token works on a protected endpoint.

**GET** `http://localhost:8080/api/protected-endpoint`

**Headers:**
```
Authorization: Bearer your_access_token_here
```

**Note:** You'll need to create a simple protected endpoint for testing, or use any existing protected endpoint in your application.

### 5. **Logout with Token Blacklisting**
**POST** `http://localhost:8080/api/auth/logout`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer your_access_token_here
```

**Body (Option 1 - With refresh token):**
```json
{
  "refreshToken": "your_refresh_token_here"
}
```

**Body (Option 2 - Without refresh token, just blacklist access token):**
```json
{}
```

**Successful Response:** `200 OK` (empty body)

**What happens:**
1. Access token extracted from Authorization header
2. Token signature added to Redis blacklist with TTL
3. Refresh token invalidated in database (if provided)
4. Subsequent requests with same access token will be rejected

### 6. **Verify Token is Blacklisted**
Try to use the same access token on a protected endpoint after logout.

**GET** `http://localhost:8080/api/protected-endpoint`

**Headers:**
```
Authorization: Bearer your_blacklisted_access_token_here
```

**Expected Response (401 Unauthorized):**
```json
{
  "error": "Token has been revoked",
  "code": "TOKEN_BLACKLISTED"
}
```

### 7. **Test Refresh Token After Logout**
Try to refresh tokens using the refresh token that was invalidated during logout.

**POST** `http://localhost:8080/api/auth/refresh`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "refreshToken": "your_invalidated_refresh_token_here"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "message": "Invalid or expired refresh token"
}
```

## Advanced Testing Scenarios

### Scenario 1: Multiple Token Blacklisting
1. Login with user A → Get Token A
2. Login with user B → Get Token B  
3. Logout user A (blacklist Token A)
4. Verify Token A is rejected (401)
5. Verify Token B still works (200)

### Scenario 2: Token Expiration vs Blacklisting
1. Login → Get token (30min expiration)
2. Logout immediately (token blacklisted)
3. Wait 1 minute
4. Try to use token → Should get `TOKEN_BLACKLISTED` (not `TOKEN_EXPIRED`)

### Scenario 3: Redis Restart Resilience
1. Login → Get token
2. Logout → Token blacklisted in Redis
3. Restart Redis: `docker-compose restart redis`
4. Try to use blacklisted token → Should work (Redis data lost, graceful fallback)

## Postman Collection Variables

Set up environment variables in Postman:

```javascript
{
  "base_url": "http://localhost:8080",
  "access_token": "",
  "refresh_token": "",
  "blacklisted_token": ""
}
```

**Test Script Example (for login):**
```javascript
pm.test("Login successful", function() {
    pm.response.to.have.status(200);
    
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.accessToken);
    pm.environment.set("refresh_token", jsonData.refreshToken);
});

pm.test("Response has tokens", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.accessToken).to.not.be.empty;
    pm.expect(jsonData.refreshToken).to.not.be.empty;
});
```

## Monitoring Redis Blacklist

Check Redis directly to see blacklisted tokens:

```bash
# Connect to Redis
docker exec -it redis-los redis-cli

# List all blacklisted tokens
KEYS jwt:blacklist:*

# Check TTL of a specific token
TTL jwt:blacklist:your_token_signature

# Get token value
GET jwt:blacklist:your_token_signature
```

## Expected Behavior Summary

| Scenario | Expected Result | HTTP Code | Response Body |
|----------|----------------|-----------|---------------|
| Valid token | Access granted | 200 | Protected resource |
| Blacklisted token | Access denied | 401 | `{"error": "Token has been revoked", "code": "TOKEN_BLACKLISTED"}` |
| Expired token | Access denied | 401 | `{"error": "Token expired", "code": "TOKEN_EXPIRED"}` |
| No token | Access denied | Varies | Depends on endpoint |
| Invalid token format | Access denied | 401 | Generic error |

## Troubleshooting

1. **Redis Connection Issues:**
   - Check Redis is running: `docker ps | grep redis`
   - Check logs: `docker logs redis-los`
   - Verify connection in app logs

2. **Token Not Being Blacklisted:**
   - Check Spring Boot logs for blacklisting messages
   - Verify Redis key exists: `KEYS jwt:blacklist:*`
   - Check token extraction from Authorization header

3. **Blacklisted Token Still Works:**
   - Check JwtAuthenticationFilter is properly configured
   - Verify `isTokenBlacklisted()` method is called
   - Check Redis TTL (token might have expired naturally)

## Conclusion

The Redis token blacklisting implementation provides immediate token revocation upon logout, enhancing security for your Loan Origination System. Test all scenarios to ensure proper integration between PostgreSQL (persistent user data) and Redis (fast token blacklisting).