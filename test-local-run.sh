#!/bin/bash

# Local Test Script for Loan Origination System
# This script helps test the application locally

set -e

echo "=== Loan Origination System Local Test ==="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Check prerequisites
echo "1. Checking prerequisites..."
echo "   - Docker and Docker Compose"
if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
    print_status 0 "Docker and Docker Compose are installed"
else
    print_status 1 "Docker or Docker Compose not found"
    exit 1
fi

echo "   - Java 21"
if command -v java &> /dev/null && java -version 2>&1 | grep -q "21"; then
    print_status 0 "Java 21 is installed"
else
    echo -e "${YELLOW}⚠ Java 21 not detected. The application requires Java 21.${NC}"
fi

echo "   - Maven"
if command -v mvn &> /dev/null; then
    print_status 0 "Maven is installed"
else
    print_status 1 "Maven not found"
    exit 1
fi

echo ""
echo "2. Starting Docker Compose services (PostgreSQL and Redis)..."
docker-compose down > /dev/null 2>&1
docker-compose up -d

# Wait for services to be ready
echo "   Waiting for services to be ready..."
sleep 10

# Check if PostgreSQL is running
if docker-compose ps | grep -q "postgres-los.*Up"; then
    print_status 0 "PostgreSQL is running"
else
    print_status 1 "PostgreSQL failed to start"
    docker-compose logs postgres
    exit 1
fi

# Check if Redis is running
if docker-compose ps | grep -q "redis-los.*Up"; then
    print_status 0 "Redis is running"
else
    print_status 1 "Redis failed to start"
    docker-compose logs redis
    exit 1
fi

echo ""
echo "3. Testing database connection..."
if docker exec postgres-los psql -U los_user -d los_db -c "SELECT 1;" > /dev/null 2>&1; then
    print_status 0 "Database connection successful"
else
    print_status 1 "Database connection failed"
    echo "   Creating database if needed..."
    docker exec postgres-los psql -U los_user -d postgres -c "CREATE DATABASE los_db;" 2>/dev/null || true
fi

echo ""
echo "4. Building the application..."
if mvn clean compile -q; then
    print_status 0 "Application compiled successfully"
else
    print_status 1 "Application compilation failed"
    echo "   Running with more details:"
    mvn clean compile
    exit 1
fi

echo ""
echo "5. Starting the Spring Boot application (in background)..."
echo "   Note: The application may have Hibernate query issues that need to be fixed separately."
echo "   Starting with timeout of 30 seconds to check initial startup..."

# Start the application in background with timeout
timeout 30 mvn spring-boot:run -Dspring-boot.run.profiles=local -q &
APP_PID=$!

# Wait a bit and check if it's still running
sleep 15

if ps -p $APP_PID > /dev/null 2>&1; then
    print_status 0 "Application is running (PID: $APP_PID)"
    
    echo ""
    echo "6. Testing API endpoints..."
    echo "   Waiting for application to be fully ready..."
    sleep 5
    
    # Try to access health endpoint
    if curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_status 0 "Health endpoint is accessible"
        echo "   Health check response:"
        curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
    else
        echo -e "${YELLOW}⚠ Health endpoint not accessible (application may still be starting)${NC}"
    fi
    
    # Kill the application
    echo ""
    echo "7. Stopping the application..."
    kill $APP_PID 2>/dev/null || true
    wait $APP_PID 2>/dev/null || true
    print_status 0 "Application stopped"
else
    echo -e "${YELLOW}⚠ Application failed to start or stopped prematurely${NC}"
    echo "   This may be due to Hibernate query errors in the application code."
    echo "   Check the logs above for details."
fi

echo ""
echo "8. Testing Docker image build with Jib..."
if mvn compile jib:dockerBuild -Ddocker.image.prefix=test-local -q; then
    print_status 0 "Docker image built successfully with Jib"
    echo "   Image: test-local/los:0.0.1-SNAPSHOT"
    
    # List the image
    echo "   Available test images:"
    docker images | grep test-local/los
else
    print_status 1 "Docker image build failed"
fi

echo ""
echo "=== Test Summary ==="
echo "The Loan Origination System has been configured for:"
echo "1. ✅ Docker Hub deployment with Jib (see DEPLOYMENT_GUIDE.md)"
echo "2. ✅ Local development with Docker Compose"
echo "3. ⚠  Application has Hibernate query issues that need fixing"
echo ""
echo "Next steps:"
echo "1. Fix the Hibernate query errors in the application code"
echo "2. Run './test-local-run.sh' again to verify"
echo "3. Deploy to Docker Hub: './deploy-to-dockerhub.sh YOUR_DOCKERHUB_USERNAME'"
echo ""
echo "For Docker Hub deployment details, see DEPLOYMENT_GUIDE.md"