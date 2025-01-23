# Use a base image that supports Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file to the working directory
COPY target/post-service-0.0.1-SNAPSHOT.jar post-service.jar

# Expose the port the application will run on
EXPOSE 9002

# Run the jar file
ENTRYPOINT ["java", "-jar", "post-service.jar"]
