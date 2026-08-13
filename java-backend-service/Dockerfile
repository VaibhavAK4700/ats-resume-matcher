FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copy pom.xml first and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests

# Final runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]