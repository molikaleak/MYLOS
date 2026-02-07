# Postman Testing Guide - Loan Origination System

This guide provides comprehensive testing instructions for the entire Loan Origination System API, including authentication, customer management, loan applications, and product catalog.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Authentication Endpoints](#authentication-endpoints)
4. [Customer Management Endpoints](#customer-management-endpoints)
5. [Loan Application Endpoints](#loan-application-endpoints)
6. [Product Catalog Endpoints](#product-catalog-endpoints)
7. [Complete Test Scenarios](#complete-test-scenarios)
8. [Postman Collection Setup](#postman-collection-setup)
9. [Environment Variables](#environment-variables)
10. [Test Scripts](#test-scripts)
11. [Error Handling](#error-handling)
12. [Monitoring and Debugging](#monitoring-and-debugging)
13. [Troubleshooting](#troubleshooting)

## Prerequisites

### 1. **Start Services:**
```bash
docker-compose up -d
```
This starts:
- PostgreSQL (port 5432) - Database
- Redis (port 6379) - Token blacklisting and caching

### 2. **Start Spring Boot Application:**
```bash
./mvnw spring-boot:run
```
Application runs on `http://localhost:8080`

### 3. **Import Postman Collection:**
Import the `Loan-Origination-System.postman_collection.json` file into Postman.

### 4. **Set Up Environment:**
Create a new environment in Postman with the following variables:
- `baseUrl`: `http://localhost:8080`
- `access_token`: (leave empty, will be set automatically)
- `refresh_token`: (leave empty, will be set automatically)
- `customer_id`: (leave empty, will be set automatically)
- `loan_application_id`: (leave empty, will be set automatically)

## Quick Start

### Step 1: Test Authentication
1. Run the **Health Check** request to verify the service is running
2. Run the **Login** request with test credentials
3. Save the returned tokens (automatically saved by test scripts)

### Step 2: Test Customer Management
1. Run **Create Customer** to add a test customer
2. Run **Get Customer by ID** to verify creation
3. Run **Search Customers** to find the customer

### Step 3: Test Loan Application Flow
1. Run **Create Loan Application** for the test customer
2. Run **Update Loan Application Status** to submit for review
3. Run **Approve Loan Application** (requires manager credentials)

### Step 4: Test Product Catalog
1. Run **Get All Products** to view available loan products
2. Run **Get Products for Amount** to find suitable products

## Authentication Endpoints

### 1. **Health Check**
**GET** `{{baseUrl}}/api/auth/health`

**Response:**
```json
"Authentication service is running"
```

### 2. **User Login**
**POST** `{{baseUrl}}/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Test Credentials:**
```json
{
  "usernameOrEmail": "officer.john",
  "password": "officer123"
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

**Test Script:** Automatically saves tokens to environment variables.

### 3. **Token Refresh**
**POST** `{{baseUrl}}/api/auth/refresh`

**Body:**
```json
{
  "refreshToken": "{{refresh_token}}"
}
```

**Response:** Returns new access and refresh tokens.

### 4. **Logout with Token Blacklisting**
**POST** `{{baseUrl}}/api/auth/logout`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{access_token}}
```

**Body:**
```json
{
  "refreshToken": "{{refresh_token}}"
}
```

**What happens:**
1. Access token extracted from Authorization header
2. Token signature added to Redis blacklist with TTL
3. Refresh token invalidated in database
4. Subsequent requests with same access token will be rejected

## Customer Management Endpoints

### 1. **Create Customer**
**POST** `{{baseUrl}}/api/customers`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{access_token}}
```

**Request Body:**
```json
{
  "nameEn": "John Doe",
  "nameKh": "ចន ដូ",
  "phone": "+85512345678",
  "addressId": 1,
  "email": "john.doe@example.com",
  "idNumber": "123456789",
  "idType": "PASSPORT",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "occupation": "Software Engineer",
  "monthlyIncome": 2500.00,
  "maritalStatus": "MARRIED",
  "nationality": "Cambodian"
}
```

**Response (201 Created):** Returns customer details with generated ID.

**Test Script:** Automatically saves customer ID to environment variable.

### 2. **Get Customer by ID**
**GET** `{{baseUrl}}/api/customers/{{customer_id}}`

**Headers:**
```
Authorization: Bearer {{access_token}}
```

### 3. **Get All Customers**
**GET** `{{baseUrl}}/api/customers`

### 4. **Search Customers by Name**
**GET** `{{baseUrl}}/api/customers/search?name=John`

### 5. **Update Customer**
**PUT** `{{baseUrl}}/api/customers/{{customer_id}}`

### 6. **Delete Customer**
**DELETE** `{{baseUrl}}/api/customers/{{customer_id}}`

### 7. **Get Customer Credit Score**
**GET** `{{baseUrl}}/api/customers/{{customer_id}}/credit-score`

## Loan Application Endpoints

### 1. **Create Loan Application**
**POST** `{{baseUrl}}/api/loan-applications`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{access_token}}
```

**Request Body:**
```json
{
  "customerId": {{customer_id}},
  "productId": 1,
  "branchId": 1,
  "appliedAmount": 10000.00,
  "loanTermMonths": 24,
  "purpose": "Home Renovation",
  "collateralDescription": "Land title deed No. 12345",
  "collateralValue": 20000.00,
  "remarks": "Urgent home renovation needed",
  "repaymentFrequency": "MONTHLY",
  "currencyCode": "USD"
}
```

**Response (201 Created):** Returns loan application details with generated ID.

**Test Script:** Automatically saves loan application ID to environment variable.

### 2. **Get Loan Application by ID**
**GET** `{{baseUrl}}/api/loan-applications/{{loan_application_id}}`

### 3. **Get Loan Applications by Customer**
**GET** `{{baseUrl}}/api/loan-applications/customer/{{customer_id}}`

### 4. **Get Loan Applications by Status**
**GET** `{{baseUrl}}/api/loan-applications/status/DRAFT`

### 5. **Update Loan Application Status**
**PUT** `{{baseUrl}}/api/loan-applications/{{loan_application_id}}/status?statusCode=SUBMITTED&remarks=Submitted%20for%20review`

### 6. **Approve Loan Application**
**POST** `{{baseUrl}}/api/loan-applications/{{loan_application_id}}/approve?approvedAmount=9500.00&approvedBy=manager.jane`

### 7. **Reject Loan Application**
**POST** `{{baseUrl}}/api/loan-applications/{{loan_application_id}}/reject?rejectionReason=Insufficient%20income&rejectedBy=manager.jane`

### 8. **Get Total Approved Amount by Branch**
**GET** `{{baseUrl}}/api/loan-applications/branch/1/total-approved`

## Product Catalog Endpoints

### 1. **Get All Products**
**GET** `{{baseUrl}}/api/products`

**Response:**
```json
[
  {
    "id": 1,
    "code": "PL-001",
    "name": "Personal Loan Basic",
    "description": "Basic personal loan for general purposes",
    "minAmount": 1000.00,
    "maxAmount": 5000.00,
    "tenureMonth": 12,
    "interestRate": 12.5,
    "productType": "PERSONAL_LOAN",
    "statusCode": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
]
```

### 2. **Get Active Products**
**GET** `{{baseUrl}}/api/products/active`

### 3. **Get Product by ID**
**GET** `{{baseUrl}}/api/products/1`

### 4. **Get Product by Code**
**GET** `{{baseUrl}}/api/products/code/PL-001`

### 5. **Get Products by Type**
**GET** `{{baseUrl}}/api/products/type/PERSONAL_LOAN`

### 6. **Search Products**
**GET** `{{baseUrl}}/api/products/search?search=loan`

### 7. **Get Products for Amount**
**GET** `{{baseUrl}}/api/products/for-amount?amount=15000.00`

## Complete Test Scenarios

### Scenario 1: Complete Loan Application Flow
1. **Login as Officer** - Get authentication token
2. **Create New Customer** - Register a test customer
3. **Create Loan Application** - Submit loan application for the customer
4. **Update Application Status** - Change status to SUBMITTED
5. **Login as Manager** - Use manager credentials
6. **Approve Loan Application** - Approve with reduced amount
7. **Verify Approval** - Check application status is APPROVED

### Scenario 2: Customer Management Workflow
1. **Create Multiple Customers** - Test bulk creation
2. **Search Customers** - Test search functionality
3. **Update Customer Information** - Modify customer details
4. **Calculate Credit Score** - Test credit scoring
5. **Delete Test Customer** - Clean up test data

### Scenario 3: Product Catalog Testing
1. **Browse All Products** - View complete product catalog
2. **Filter Active Products** - Test status filtering
3. **Search by Type** - Test product type filtering
4. **Find Suitable Products** - Test amount-based filtering

### Scenario 4: Error Handling Tests
1. **Invalid Credentials** - Test 401 Unauthorized
2. **Missing Required Fields** - Test 400 Bad Request
3. **Non-existent Resources** - Test 404 Not Found
4. **Invalid Token** - Test token validation
5. **Blacklisted Token** - Test logout functionality

## Postman Collection Setup

### Collection Structure
The Postman collection is organized into folders:
1. **Authentication** - Login, register, refresh, logout
2. **Customers** - All customer management endpoints
3. **Loan Applications** - Loan application lifecycle
4. **Products** - Product catalog endpoints
5. **Test Scenarios** - Complete end-to-end workflows

### Pre-request Scripts
Global pre-request script ensures environment variables are set:
```javascript
if (!pm.environment.get("baseUrl")) {
    pm.environment.set("baseUrl", "http://localhost:8080");
}
```

### Test Scripts
Global test script runs after each request:
```javascript
pm.test("Response time is acceptable", function () {
    pm.expect(pm.response.responseTime).to.be.below(1000);
});

pm.test("Response has valid JSON", function () {
    pm.response.to.have.jsonBody();
});
```

## Environment Variables

### Required Variables
```javascript
{
  "baseUrl": "http://localhost:8080",
  "access_token": "",
  "refresh_token": "",
  "customer_id": "",
  "loan_application_id": ""
}
```

### Test User Credentials
| Username | Password | Role | Use Case |
|----------|----------|------|----------|
| `admin` | `admin123` | ADMIN | Full system access |
| `officer.john` | `officer123` | OFFICER | Customer and application management |
| `manager.jane` | `manager123` | MANAGER | Loan approval |

### Test Data IDs
- **Product IDs**: 1 (Personal Loan), 2 (Home Loan), 3 (Business Loan), 4 (Education Loan)
- **Branch ID**: 1 (Main Branch)
- **Address ID**: 1 (Test Address)

## Test Scripts

### Login Test Script
```javascript
pm.test("Login successful", function() {
    pm.response.to.have.status(200);
});

pm.test("Response has access token", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.accessToken).to.not.be.empty;
    pm.expect(jsonData.refreshToken).to.not.be.empty;
    
    // Store tokens in environment variables
    pm.environment.set("access_token", jsonData.accessToken);
    pm.environment.set("refresh_token", jsonData.refreshToken);
});
```

### Create Customer Test Script
```javascript
pm.test("Customer created successfully", function() {
    pm.response.to.have.status(201);
});

pm.test("Response has customer ID", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.be.a('number');
    
    // Store customer ID for later use
    pm.environment.set("customer_id", jsonData.id);
});
```

### Create Loan Application Test Script
```javascript
pm.test("Loan application created", function() {
    pm.response.to.have.status(201);
});

pm.test("Response has application ID", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.be.a('number');
    
    // Store application ID for later use
    pm.environment.set("loan_application_id", jsonData.id);
});
```

## Error Handling

### Common HTTP Status Codes
| Code | Meaning | Typical Causes |
|------|---------|----------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., duplicate) |
| 500 | Internal Server Error | Server-side error |

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid customer data",
  "path": "/api/customers"
}
```

## Monitoring and Debugging

### Check Application Logs
```bash
# View Spring Boot application logs
tail -f logs/application.log

# View specific endpoint logs
grep "Loan application" logs/application.log
```

### Monitor Redis
```bash
# Connect to Redis
docker exec -it redis-los redis-cli

# List all blacklisted tokens
KEYS jwt:blacklist:*

# Check Redis memory usage
INFO memory

# Monitor Redis operations
MONITOR
```

### Check Database
```bash
# Connect to PostgreSQL
docker exec -it postgres-los psql -U postgres -d loan_origination

# Check customer table
SELECT * FROM t_customer LIMIT 5;

# Check loan applications
SELECT * FROM t_loan_application LIMIT 5;
```

## Troubleshooting

### 1. **Application Won't Start**
- Check if ports 8080, 5432, 6379 are available
- Verify Docker containers are running: `docker ps`
- Check application logs: `./mvnw spring-boot:run`

### 2. **Authentication Failures**
- Verify user exists in database
- Check password encryption matches
- Verify JWT secret key configuration
- Check Redis connection for token blacklisting

### 3. **Database Connection Issues**
- Verify PostgreSQL is running: `docker ps | grep postgres`
- Check connection string in `application.properties`
- Verify database credentials

### 4. **Postman Collection Issues**
- Ensure environment is selected in Postman
- Verify environment variables are set
- Check if tokens are being saved correctly
- Clear cookies and cache if needed

### 5. **Performance Issues**
- Check database query performance
- Monitor Redis cache hit rate
- Review application logs for slow operations
- Consider adding database indexes

## Best Practices

### Testing Strategy
1. **Start with Health Checks** - Verify services are running
2. **Test Authentication First** - Get valid tokens
3. **Use Test Data** - Create, read, update, delete in sequence
4. **Clean Up** - Delete test data after testing
5. **Test Error Cases** - Verify proper error responses

### Security Considerations
1. **Never commit tokens** - Keep tokens in environment variables
2. **Use different credentials** for different roles
3. **Test authorization** - Verify role-based access control
4. **Test token blacklisting** - Verify logout functionality

### Performance Testing
1. **Monitor response times** - Should be under 1 second
2. **Test concurrent requests** - Simulate multiple users
3. **Check memory usage** - Monitor for leaks
4. **Verify caching** - Redis should improve performance

## Conclusion

This Postman collection provides comprehensive testing for the Loan Origination System. Follow the test scenarios to validate all functionality, from authentication to loan approval. Use the monitoring tools to debug issues and ensure system reliability.

For additional test data, refer to `mock-data.json` which contains sample requests and responses for all endpoints.