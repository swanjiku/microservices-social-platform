# Use a base image that supports Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file to the working directory
COPY target/comment-service-0.0.1-SNAPSHOT.jar comment-service.jar

# Expose the port the application will run on
EXPOSE 9003

# Run the jar file
ENTRYPOINT ["java", "-jar", "comment-service.jar"]
