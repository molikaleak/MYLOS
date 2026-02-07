# Docker Hub Deployment with Jib

This guide explains how to deploy the Loan Origination System to Docker Hub using Jib.

## Prerequisites

1. Docker installed and running
2. Docker Hub account
3. Maven 3.6+
4. Java 21

## Configuration

### 1. Update Docker Hub Username

Edit `pom.xml` and replace `your-dockerhub-username` with your actual Docker Hub username:

```xml
<docker.image.prefix>your-dockerhub-username</docker.image.prefix>
```

Alternatively, you can override it via command line:
```bash
mvn jib:build -Ddocker.image.prefix=your-dockerhub-username
```

### 2. Login to Docker Hub

Ensure you're logged in to Docker Hub:
```bash
docker login
```

## Building and Pushing to Docker Hub

### Option A: Build and push in one step (recommended)

```bash
mvn compile jib:build -Ddocker.image.prefix=your-dockerhub-username
```

This will:
1. Build the Docker image
2. Push it to Docker Hub as `docker.io/your-dockerhub-username/los:0.0.1-SNAPSHOT`
3. Also tag it as `latest`

### Option B: Build locally first, then push

```bash
# Build to local Docker daemon
mvn compile jib:dockerBuild -Ddocker.image.prefix=your-dockerhub-username

# Push to Docker Hub (if you want to push the local image)
docker push your-dockerhub-username/los:0.0.1-SNAPSHOT
docker push your-dockerhub-username/los:latest
```

## Image Details

- **Base Image**: `eclipse-temurin:21-jre-alpine` (Java 21, Alpine Linux)
- **Platform**: `linux/arm64` (configured for Apple Silicon)
- **Port**: 8080 exposed
- **Environment**: `SPRING_PROFILES_ACTIVE=prod`
- **Entrypoint**: Automatically runs the Spring Boot application

## Running the Container

```bash
docker run -p 8080:8080 your-dockerhub-username/los:latest
```

The application will be available at `http://localhost:8080`

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/docker-publish.yml`:

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build and Push with Jib
        env:
          DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }}
          DOCKER_PASSWORD: ${{ secrets.DOCKER_PASSWORD }}
        run: |
          mvn compile jib:build \
            -Ddocker.image.prefix=$DOCKER_USERNAME \
            -Djib.to.auth.username=$DOCKER_USERNAME \
            -Djib.to.auth.password=$DOCKER_PASSWORD
```

### Environment Variables for Authentication

If you need to authenticate with different credentials:

```bash
mvn compile jib:build \
  -Ddocker.image.prefix=your-dockerhub-username \
  -Djib.to.auth.username=your-dockerhub-username \
  -Djib.to.auth.password=your-dockerhub-password
```

## Troubleshooting

### 1. Platform Mismatch Error

If you see `The configured platforms don't match the Docker Engine's OS and architecture`, update the platform configuration in `pom.xml`:

```xml
<platforms>
  <platform>
    <architecture>amd64</architecture> <!-- or arm64 -->
    <os>linux</os>
  </platform>
</platforms>
```

### 2. Authentication Errors

Ensure Docker credentials are set:
```bash
echo $DOCKER_PASSWORD | docker login --username $DOCKER_USERNAME --password-stdin
```

### 3. Java Version Mismatch

Ensure your Java version matches the base image. The project is configured for Java 21.

### 4. Image Name Format

Docker Hub image names must be lowercase. The artifactId has been changed to `los` to comply.

## References

- [Jib Maven Plugin Documentation](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin)
- [Docker Hub Documentation](https://docs.docker.com/docker-hub/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
