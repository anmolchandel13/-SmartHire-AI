# ==========================================
# STAGE 1: Build Java Project
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy POM and download dependencies first to optimize Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
RUN mvn package -DskipTests

# ==========================================
# STAGE 2: Lightweight Runtime JRE
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy output JAR from the builder stage
COPY --from=builder /app/target/smarthire-backend-*.jar app.jar

# Expose backend REST port
EXPOSE 8080

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
