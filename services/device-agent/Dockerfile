# Multi-stage build for Device Agent
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre

# Install system tools needed for device control
RUN apt-get update && apt-get install -y \
    curl \
    pulseaudio-utils \
    alsa-utils \
    playerctl \
    gnome-screenshot \
    scrot \
    imagemagick \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r smartjarvis && useradd -r -g smartjarvis smartjarvis

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/device-agent-*.jar device-agent.jar

# Create screenshot directory
RUN mkdir -p /tmp/smartjarvis/screenshots && \
    chown -R smartjarvis:smartjarvis /app /tmp/smartjarvis

# Switch to non-root user
USER smartjarvis

# Expose port
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# JVM optimizations
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Note: For real device control, this service should run on host network
# docker run --network=host --privileged -v /tmp:/host/tmp

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar device-agent.jar"]
