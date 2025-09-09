# Multi-stage build for Voice Gateway service
FROM maven:3.9-openjdk-21-slim AS build

WORKDIR /app

# Copy pom.xml and shared schemas for dependencies
COPY pom.xml .
COPY ../../shared/avro-schemas ../../shared/avro-schemas
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:21-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r smartjarvis && useradd -r -g smartjarvis smartjarvis

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/voice-gateway-*.jar voice-gateway.jar

# Change ownership to non-root user
RUN chown -R smartjarvis:smartjarvis /app

# Switch to non-root user
USER smartjarvis

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimizations for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar voice-gateway.jar"]
