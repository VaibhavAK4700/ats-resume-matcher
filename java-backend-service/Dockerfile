# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Minimal Runtime stage (ARM64 / Mac compatible)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create non-root user for security compliance
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R javauser:javauser /app

USER javauser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]