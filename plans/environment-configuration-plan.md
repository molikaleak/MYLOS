# Environment Configuration and Secret Management Plan

## Executive Summary
This document outlines the strategy for implementing secure environment variable management in the Loan Origination System. The current system has hardcoded secrets in `application.properties` which poses security risks for production deployment.

## Current Security Issues Identified

### 1. **Hardcoded Credentials in [`application.properties`](src/main/resources/application.properties)**
```properties
# Database Configuration (Lines 4-6)
spring.datasource.url=jdbc:postgresql://localhost:5432/loan_db
spring.datasource.username=loan_user
spring.datasource.password=secret

# JWT Configuration (Line 16)
app.jwt.secret-key=your-256-bit-secret-key-change-this-in-production-with-a-strong-random-key
```

### 2. **Missing Environment Variable Support**
- Empty `.env` file not being utilized
- No environment-specific configuration files
- `.gitignore` doesn't exclude sensitive files

### 3. **Configuration Structure Issues**
- Single `application.properties` for all environments
- No separation between development, staging, and production configurations

## Proposed Environment Strategy

### Multi-Environment Configuration Approach

```mermaid
graph TB
    subgraph "Configuration Sources"
        EnvVars[Environment Variables]
        DotEnv[.env files]
        AppProps[application-{profile}.properties]
        DefaultProps[application.properties]
    end
    
    subgraph "Spring Boot Configuration"
        SB[Spring Boot App]
        ConfigProps[ConfigurationProperties]
    end
    
    subgraph "Security Layers"
        Vault[External Vault - Optional]
        K8s[Kubernetes Secrets - Optional]
    end
    
    EnvVars --> SB
    DotEnv --> SB
    AppProps --> SB
    DefaultProps --> SB
    Vault -.-> EnvVars
    K8s -.-> EnvVars
    SB --> ConfigProps
```

### Configuration Priority (Highest to Lowest)
1. **Environment Variables** (System/OS level)
2. **`.env` file** (Project root, loaded via dotenv)
3. **`application-{profile}.properties`** (Profile-specific)
4. **`application.properties`** (Default configuration)

## Implementation Plan

### Phase 1: Foundation Setup

#### 1.1 Update `.gitignore` to Exclude Sensitive Files
```gitignore
### Environment Configuration Files ###
.env
.env.*
!.env.example
.env.local
.env.development
.env.staging
.env.production
.env.test

### Application Configuration ###
application-local.properties
application-dev.properties
application-staging.properties
application-prod.properties

### Secrets and Credentials ###
*.key
*.pem
*.crt
*.cert
secrets/
credentials/
```

#### 1.2 Create Environment Template Files
- **`.env.example`**: Template with placeholder values
- **`.env.development`**: Local development configuration
- **`.env.production`**: Production configuration template

#### 1.3 Add Dotenv Dependency to `pom.xml`
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Phase 2: Configuration Refactoring

#### 2.1 Create Environment-Specific Property Files
- `application-local.properties` - Local development
- `application-dev.properties` - Development environment
- `application-staging.properties` - Staging environment  
- `application-prod.properties` - Production environment

#### 2.2 Refactor `application.properties` to Use Placeholders
```properties
# Database Configuration
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/loan_db}
spring.datasource.username=${DATABASE_USERNAME:loan_user}
spring.datasource.password=${DATABASE_PASSWORD:secret}

# JWT Configuration
app.jwt.secret-key=${JWT_SECRET_KEY:your-256-bit-secret-key-change-this-in-production}
app.jwt.access-token-expiration=${JWT_ACCESS_EXPIRATION:30m}
app.jwt.refresh-token-expiration=${JWT_REFRESH_EXPIRATION:7d}
app.jwt.issuer=${JWT_ISSUER:loan-origination-system}
app.jwt.audience=${JWT_AUDIENCE:loan-origination-client}
```

#### 2.3 Create Dotenv Configuration Loader
```java
// DotenvConfig.java - Load .env file before Spring Boot starts
@Configuration
public class DotenvConfig {
    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .ignoreIfMissing()
            .load();
        
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
```

### Phase 3: JWT Properties Enhancement

#### 3.1 Update [`JwtProperties.java`](src/main/java/com/example/los/infrastructure/config/JwtProperties.java)
```java
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtProperties {
    
    @NotBlank(message = "JWT secret key must not be blank")
    private String secretKey;
    
    @NotNull
    private Duration accessTokenExpiration = Duration.ofMinutes(30);
    
    @NotNull
    private Duration refreshTokenExpiration = Duration.ofDays(7);
    
    @NotBlank
    private String issuer = "loan-origination-system";
    
    @NotBlank
    private String audience = "loan-origination-client";
    
    // Add validation for production
    @PostConstruct
    public void validateForProduction() {
        if (isProduction() && "your-256-bit-secret-key-change-this-in-production".equals(secretKey)) {
            throw new IllegalStateException(
                "Default JWT secret key detected in production. " +
                "Set JWT_SECRET_KEY environment variable."
            );
        }
    }
    
    private boolean isProduction() {
        String profile = System.getProperty("spring.profiles.active", "");
        return "prod".equals(profile) || "production".equals(profile);
    }
}
```

### Phase 4: Sample Configuration Files

#### 4.1 `.env.example` Template
```env
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/loan_db
DATABASE_USERNAME=loan_user
DATABASE_PASSWORD=your_secure_password_here

# JWT Configuration
JWT_SECRET_KEY=generate-a-strong-256-bit-secret-key-here
JWT_ACCESS_EXPIRATION=30m
JWT_REFRESH_EXPIRATION=7d
JWT_ISSUER=loan-origination-system
JWT_AUDIENCE=loan-origination-client

# Server Configuration
SERVER_PORT=8080

# Redis Configuration (if used)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka Configuration (if used)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

#### 4.2 `application-local.properties` Example
```properties
# Spring Profile
spring.profiles.active=local

# Database Configuration (uses .env values)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# Development-specific settings
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.com.example.los=DEBUG
```

#### 4.3 `application-prod.properties` Example
```properties
# Spring Profile
spring.profiles.active=prod

# Database Configuration (uses environment variables)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# Production-specific settings
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate

# Security
management.endpoints.web.exposure.include=health,info,metrics

# Logging
logging.level.com.example.los=INFO
logging.file.name=/var/log/loan-origination/app.log
```

## Deployment Instructions

### Local Development
1. Copy `.env.example` to `.env.local`
2. Update values in `.env.local` for local environment
3. Run with: `./mvnw spring-boot:run -Dspring.profiles.active=local`

### Production Deployment

#### Option A: Traditional Deployment
```bash
# Set environment variables
export DATABASE_URL=jdbc:postgresql://prod-db:5432/loan_prod
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=$(cat /secrets/db-password)
export JWT_SECRET_KEY=$(cat /secrets/jwt-key)

# Run application
java -Dspring.profiles.active=prod -jar loan-origination.jar
```

#### Option B: Docker Deployment
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/loan-origination.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
```

```bash
# Run with environment variables
docker run -d \
  -e DATABASE_URL=jdbc:postgresql://prod-db:5432/loan_prod \
  -e DATABASE_USERNAME=prod_user \
  -e DATABASE_PASSWORD=secret \
  -e JWT_SECRET_KEY=your-secret-key \
  -p 8080:8080 \
  loan-origination:latest
```

#### Option C: Kubernetes Deployment
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: loan-origination-secrets
type: Opaque
data:
  database-password: c2VjcmV0 # base64 encoded
  jwt-secret-key: eW91ci1zZWNyZXQta2V5 # base64 encoded
---
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: app
        image: loan-origination:latest
        env:
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: loan-origination-secrets
              key: database-password
        - name: JWT_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: loan-origination-secrets
              key: jwt-secret-key
```

## Security Best Practices

### 1. **Secret Rotation**
- Implement automatic secret rotation for JWT keys
- Use Kubernetes Secrets or HashiCorp Vault for dynamic secrets
- Set up alerts for expiring certificates

### 2. **Access Control**
- Restrict access to production environment variables
- Use different credentials for each environment
- Implement principle of least privilege

### 3. **Monitoring and Auditing**
- Log configuration loading (without exposing secrets)
- Monitor for default credential usage
- Set up alerts for missing environment variables

### 4. **Development Workflow**
- Never commit `.env` files to version control
- Use different JWT keys for each environment
- Implement pre-commit hooks to prevent secret leakage

## Testing Strategy

### Unit Tests
```java
@Test
public void testJwtPropertiesValidation() {
    JwtProperties properties = new JwtProperties();
    properties.setSecretKey("test-key");
    
    // Test default values
    assertEquals(Duration.ofMinutes(30), properties.getAccessTokenExpiration());
    assertEquals(Duration.ofDays(7), properties.getRefreshTokenExpiration());
}

@Test
public void testEnvironmentVariableResolution() {
    System.setProperty("DATABASE_URL", "test-url");
    // Test that properties resolve correctly
}
```

### Integration Tests
- Test configuration loading with different profiles
- Verify environment variable precedence
- Test secret masking in logs

## Migration Checklist

- [ ] Update `.gitignore` with environment file exclusions
- [ ] Create `.env.example` template
- [ ] Add dotenv-java dependency to `pom.xml`
- [ ] Refactor `application.properties` to use placeholders
- [ ] Create environment-specific property files
- [ ] Implement Dotenv configuration loader
- [ ] Update JwtProperties with validation
- [ ] Create deployment documentation
- [ ] Test configuration across all environments
- [ ] Update CI/CD pipeline to handle secrets

## Risk Mitigation

### 1. **Backward Compatibility**
- Maintain existing `application.properties` as fallback
- Use default values in placeholders for smooth migration
- Phase implementation to avoid breaking changes

### 2. **Secret Exposure Prevention**
- Add pre-commit hooks to detect secrets
- Implement secret scanning in CI/CD
- Use git-secrets or similar tools

### 3. **Configuration Validation**
- Add startup validation for required environment variables
- Implement health checks for configuration
- Create configuration test suite

## Conclusion

Implementing proper environment variable management is critical for secure production deployment. This plan provides a comprehensive approach that:

1. **Eliminates hardcoded secrets** from version control
2. **Supports multiple environments** with appropriate configurations
3. **Maintains backward compatibility** during migration
4. **Follows security best practices** for secret management
5. **Provides clear deployment instructions** for various platforms

The implementation should be phased to minimize disruption while improving the security posture of the Loan Origination System.