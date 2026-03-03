# ==========================================
# Stage 1: Build the Payment Service
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

# ==========================================
# Stage 2: Run the Payment Service
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/payment-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]