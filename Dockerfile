# Multi-stage build to produce a lightweight JVM container for bitaxe-exporter

# 1) Build stage: use JDK to compile + package the Spring Boot app
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

# Copy project sources
COPY . .

# Build fat JAR (Spring Boot repackage)
RUN ./mvnw -B -e -DskipTests package

# 2) Runtime stage: use a small JRE image
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

# Copy the fat JAR from the build stage
# Adjust the jar name if you change version/artifactId
COPY --from=build /workspace/target/bitaxe-exporter-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose HTTP port
EXPOSE 8080

# Your exporter config
ENV BITAXE_BASE_URL=http://192.168.178.91

# Run as non-root user
RUN addgroup -S app && adduser -S app -G app \
  && chown -R app:app /app
USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
