#!/bin/bash

# Deployment script for Loan Origination System to Docker Hub
# Usage: ./deploy-to-dockerhub.sh [DOCKER_HUB_USERNAME]

set -e

# Default values
DOCKER_HUB_USERNAME="molikaa"
IMAGE_NAME="los"
VERSION="0.0.1-SNAPSHOT"

echo "=== Deploying Loan Origination System to Docker Hub ==="
echo "Docker Hub Username: $DOCKER_HUB_USERNAME"
echo "Image: $DOCKER_HUB_USERNAME/$IMAGE_NAME:$VERSION"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "Error: Docker is not running. Please start Docker daemon."
    exit 1
fi

# Check if logged in to Docker Hub
if ! docker system info | grep -q "Username"; then
    echo "Warning: Not logged in to Docker Hub. Please run 'docker login' first."
    read -p "Do you want to login now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker login
    else
        echo "Proceeding without login (push may fail)"
    fi
fi

echo "Building and pushing image with Jib..."
mvn compile jib:build \
    -Ddocker.image.prefix="$DOCKER_HUB_USERNAME" \
    -Djib.to.auth.username="$DOCKER_HUB_USERNAME" \
    -Djib.to.auth.password="$DOCKER_PASSWORD"

echo ""
echo "=== Deployment Complete ==="
echo "Image pushed successfully:"
echo "  - $DOCKER_HUB_USERNAME/$IMAGE_NAME:$VERSION"
echo "  - $DOCKER_HUB_USERNAME/$IMAGE_NAME:latest"
echo ""
echo "To run the container locally:"
echo "  docker run -p 8080:8080 $DOCKER_HUB_USERNAME/$IMAGE_NAME:latest"
echo ""
echo "To pull and run on another machine:"
echo "  docker pull $DOCKER_HUB_USERNAME/$IMAGE_NAME:latest"
echo "  docker run -p 8080:8080 $DOCKER_HUB_USERNAME/$IMAGE_NAME:latest"