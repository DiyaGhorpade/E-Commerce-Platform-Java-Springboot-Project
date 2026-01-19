# Use a lightweight Java 21 runtime
FROM eclipse-temurin:21-jdk-alpine

# Set a temporary volume
VOLUME /tmp

# Copy the JAR file from the target folder into the container
COPY target/*.jar app.jar

# Run the JAR file
ENTRYPOINT ["java","-jar","/app.jar"]