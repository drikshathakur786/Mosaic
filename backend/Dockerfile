# =============================================================================
# Stage 1: Build the application artifact with Maven
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven POM and pre-fetch dependencies for better Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================================================
# Stage 2: Minimal production runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Create a non-privileged user and group for security
RUN addgroup -S mosaic && adduser -S mosaic -G mosaic

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /build/target/mosaic-backend-*.jar app.jar

# Adjust permissions
RUN chown -R mosaic:mosaic /app

USER mosaic

EXPOSE 8080

# Configure JVM flags optimized for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
