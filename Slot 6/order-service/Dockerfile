# Use official eclipse-temurin image with maven for building
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline
# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Use JRE image for running
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
