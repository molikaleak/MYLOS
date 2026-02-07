# Loan Origination System - REST API Plan

## Overview
This document outlines the REST API design for the Loan Origination System. It includes all endpoints, their HTTP methods, request/response structures, and example data for testing.

## Base URL
`http://localhost:8080/api`

## Authentication
All endpoints except `/api/auth/**` require a valid JWT token in the Authorization header:
```
Authorization: Bearer <access_token>
```

## API Endpoints Summary

### 1. Authentication Controller (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST   | `/register` | Register new user | No |
| POST   | `/login` | User login | No |
| POST   | `/refresh` | Refresh access token | No |
| POST   | `/logout` | Logout user (blacklist token) | Yes |
| GET    | `/health` | Service health check | No |

### 2. Customer Controller (`/api/customers`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST   | `/` | Create new customer | Yes |
| GET    | `/` | Get all customers | Yes |
| GET    | `/{id}` | Get customer by ID | Yes |
| GET    | `/search` | Search customers by name | Yes |
| GET    | `/phone/{phone}` | Get customer by phone | Yes |
| PUT    | `/{id}` | Update customer | Yes |
| DELETE | `/{id}` | Delete customer | Yes |
| GET    | `/count` | Count total customers | Yes |
| GET    | `/created-between` | Get customers created between dates | Yes |
| GET    | `/{id}/credit-score` | Calculate customer credit score | Yes |
| GET    | `/health` | Service health check | Yes |

### 3. Loan Application Controller (`/api/loan-applications`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST   | `/` | Create new loan application | Yes |
| GET    | `/{id}` | Get loan application by ID | Yes |
| GET    | `/customer/{customerId}` | Get loan applications by customer | Yes |
| GET    | `/status/{statusCode}` | Get loan applications by status | Yes |
| PUT    | `/{id}/status` | Update loan application status | Yes |
| POST   | `/{id}/approve` | Approve loan application | Yes |
| POST   | `/{id}/reject` | Reject loan application | Yes |
| DELETE | `/{id}` | Delete loan application | Yes |
| GET    | `/branch/{branchId}/total-approved` | Get total approved amount by branch | Yes |
| GET    | `/status/{statusCode}/count-since` | Count applications by status since date | Yes |
| GET    | `/health` | Service health check | Yes |

### 4. Product Controller (`/api/products`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET    | `/` | Get all products | Yes |
| GET    | `/active` | Get active products | Yes |
| GET    | `/{id}` | Get product by ID | Yes |
| GET    | `/code/{productCode}` | Get product by code | Yes |
| GET    | `/type/{productType}` | Get products by type | Yes |
| GET    | `/search` | Search products | Yes |
| GET    | `/for-amount` | Get products suitable for amount | Yes |
| GET    | `/count/active` | Count active products | Yes |
| GET    | `/health` | Service health check | Yes |

## Detailed Endpoint Specifications

### Authentication Endpoints

#### 1.1 POST `/api/auth/register`
**Request Body:**
```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "phone": "+85512345678",
  "password": "SecurePass123!",
  "branchId": 1,
  "roleCode": "OFFICER"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 1800,
  "message": "Registration successful"
}
```

#### 1.2 POST `/api/auth/login`
**Request Body:**
```json
{
  "usernameOrEmail": "john.doe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 1800,
  "message": "Login successful"
}
```

### Customer Endpoints

#### 2.1 POST `/api/customers`
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

**Response (201 Created):**
```json
{
  "id": 1,
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
  "nationality": "Cambodian",
  "createdAt": "2024-01-15T08:30:00Z",
  "updatedAt": "2024-01-15T08:30:00Z",
  "totalLoanApplications": 0,
  "activeLoans": 0,
  "totalLoanAmount": 0.00
}
```

#### 2.2 GET `/api/customers/{id}`
**Response (200 OK):**
```json
{
  "id": 1,
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
  "nationality": "Cambodian",
  "createdAt": "2024-01-15T08:30:00Z",
  "updatedAt": "2024-01-15T08:30:00Z",
  "totalLoanApplications": 2,
  "activeLoans": 1,
  "totalLoanAmount": 15000.00
}
```

### Loan Application Endpoints

#### 3.1 POST `/api/loan-applications`
**Request Body:**
```json
{
  "customerId": 1,
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

**Response (201 Created):**
```json
{
  "id": 1,
  "customerId": 1,
  "productId": 1,
  "branchId": 1,
  "applicationNumber": "LA-2024-001",
  "appliedAmount": 10000.00,
  "approvedAmount": null,
  "loanTermMonths": 24,
  "purpose": "Home Renovation",
  "collateralDescription": "Land title deed No. 12345",
  "collateralValue": 20000.00,
  "statusCode": "DRAFT",
  "statusDescription": "Application in draft status",
  "remarks": "Urgent home renovation needed",
  "repaymentFrequency": "MONTHLY",
  "currencyCode": "USD",
  "createdAt": "2024-01-15T09:00:00Z",
  "updatedAt": "2024-01-15T09:00:00Z",
  "createdBy": "john.doe",
  "updatedBy": "john.doe"
}
```

#### 3.2 POST `/api/loan-applications/{id}/approve`
**Request Parameters:**
- `approvedAmount`: 9500.00
- `approvedBy`: "manager.jane"

**Response (200 OK):**
```json
{
  "id": 1,
  "customerId": 1,
  "productId": 1,
  "branchId": 1,
  "applicationNumber": "LA-2024-001",
  "appliedAmount": 10000.00,
  "approvedAmount": 9500.00,
  "loanTermMonths": 24,
  "purpose": "Home Renovation",
  "collateralDescription": "Land title deed No. 12345",
  "collateralValue": 20000.00,
  "statusCode": "APPROVED",
  "statusDescription": "Application approved by manager",
  "remarks": "Approved with reduced amount",
  "repaymentFrequency": "MONTHLY",
  "currencyCode": "USD",
  "createdAt": "2024-01-15T09:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "createdBy": "john.doe",
  "updatedBy": "manager.jane"
}
```

### Product Endpoints

#### 4.1 GET `/api/products`
**Response (200 OK):**
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
  },
  {
    "id": 2,
    "code": "HL-001",
    "name": "Home Loan Standard",
    "description": "Standard home loan for property purchase",
    "minAmount": 10000.00,
    "maxAmount": 100000.00,
    "tenureMonth": 240,
    "interestRate": 8.5,
    "productType": "HOME_LOAN",
    "statusCode": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
]
```

#### 4.2 GET `/api/products/for-amount?amount=15000.00`
**Response (200 OK):**
```json
[
  {
    "id": 2,
    "code": "HL-001",
    "name": "Home Loan Standard",
    "description": "Standard home loan for property purchase",
    "minAmount": 10000.00,
    "maxAmount": 100000.00,
    "tenureMonth": 240,
    "interestRate": 8.5,
    "productType": "HOME_LOAN",
    "statusCode": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
]
```

## Mock Data for Testing

### Test Users
| Username | Password | Role | Branch |
|----------|----------|------|--------|
| `admin` | `admin123` | ADMIN | 1 |
| `officer.john` | `officer123` | OFFICER | 1 |
| `manager.jane` | `manager123` | MANAGER | 1 |
| `customer.smith` | `customer123` | CUSTOMER | 1 |

### Test Customers
1. **John Doe** - Software Engineer, Monthly Income: $2500
2. **Jane Smith** - Business Owner, Monthly Income: $5000  
3. **David Chen** - Teacher, Monthly Income: $1200
4. **Sopha Kim** - Nurse, Monthly Income: $1800

### Test Products
1. **Personal Loan Basic** - $1,000 to $5,000, 12 months, 12.5% interest
2. **Home Loan Standard** - $10,000 to $100,000, 240 months, 8.5% interest
3. **Business Loan Pro** - $5,000 to $50,000, 60 months, 10.5% interest
4. **Education Loan** - $500 to $10,000, 48 months, 7.5% interest

### Test Loan Applications
1. **LA-2024-001** - John Doe, Home Loan, $10,000, Status: APPROVED
2. **LA-2024-002** - Jane Smith, Business Loan, $25,000, Status: UNDER_REVIEW
3. **LA-2024-003** - David Chen, Personal Loan, $3,000, Status: REJECTED
4. **LA-2024-004** - Sopha Kim, Education Loan, $5,000, Status: DRAFT

## Testing Workflow

### Step 1: Authentication
1. Register a new user or use existing credentials
2. Login to obtain access token
3. Save token for subsequent requests

### Step 2: Customer Management
1. Create a new customer
2. Retrieve customer by ID
3. Search customers by name
4. Update customer information
5. Calculate credit score

### Step 3: Product Browsing
1. Get all products
2. Get active products
3. Search products by type
4. Find products suitable for loan amount

### Step 4: Loan Application Process
1. Create a new loan application
2. View application by ID
3. View applications by customer
4. Update application status
5. Approve/reject application
6. View branch statistics

### Step 5: Cleanup (Optional)
1. Delete test loan applications
2. Delete test customers

## Error Handling

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate)
- `500 Internal Server Error` - Server-side error

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

## Security Considerations

1. **Authentication**: JWT tokens with 30-minute expiration
2. **Authorization**: Role-based access control (RBAC)
3. **Token Blacklisting**: Redis-based token revocation on logout
4. **Input Validation**: All request data validated
5. **SQL Injection Prevention**: Prepared statements via JPA
6. **XSS Protection**: Input sanitization
7. **CORS Configuration**: Restricted to trusted origins

## Performance Considerations

1. **Pagination**: Large result sets should be paginated
2. **Caching**: Frequently accessed data cached in Redis
3. **Database Indexing**: Critical fields indexed for performance
4. **Connection Pooling**: Optimized database connections
5. **Async Processing**: Long-running operations handled asynchronously

## Versioning Strategy
- Current API version: v1
- Version included in URL path: `/api/v1/...`
- Backward compatibility maintained for minor versions
- Deprecated endpoints marked with `@Deprecated` annotation

## Monitoring and Logging
- All API calls logged with request/response details
- Performance metrics collected for each endpoint
- Error logs include stack traces for debugging
- Audit logs for sensitive operations (create, update, delete)

## Postman Collection Structure
The Postman collection will be organized into folders:
1. **Authentication** - Login, register, refresh, logout
2. **Customers** - All customer management endpoints
3. **Loan Applications** - Loan application lifecycle
4. **Products** - Product catalog endpoints
5. **Health Checks** - Service health endpoints

Each request will include:
- Proper headers (Content-Type, Authorization)
- Example request body
- Test scripts to validate responses
- Environment variables for tokens and IDs