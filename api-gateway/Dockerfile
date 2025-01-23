# Use the official OpenJDK 17 image as a base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

# Expose the port the API Gateway will run on
EXPOSE 9000

# Run the application
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]

