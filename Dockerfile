# Multi-stage Dockerfile for Suitability Engine
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -DskipITs=true -B

# Stage 2: Runtime (Distroless)
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/target/suitability-engine-*.jar /app/app.jar

# Non-root user (distroless uses nonroot user by default)
USER nonroot:nonroot

# JVM options for container environment
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Run
ENTRYPOINT ["java", "-jar", "/app/app.jar"]