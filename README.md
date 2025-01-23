# Microservices Architecture

## Overview

This project is a microservices platform designed to manage user authentication, post management, and comments, leveraging modular services. It features secure JWT-based authentication and seamless inter-service communication, ensuring scalability and maintainability. Each service is independently deployable, and they work together to provide a complete solution for handling users, posts, and comments.

## Features

- **User Authentication**: A dedicated service for user authentication, using JWT for secure token-based authentication.
- **Post Management**: A service that handles the creation, updating, and retrieval of posts.
- **Comment Management**: A service for managing user comments on posts.
- **JWT Authentication**: Secure JSON Web Token (JWT) authentication for user login and inter-service communication.
- **Inter-Service Communication**: Services communicate with each other using HTTP and REST APIs, ensuring a loosely coupled architecture.

## Architecture

This platform follows a microservices architecture pattern, where each component (User Authentication, Post Management, and Comment Management) is built as an independent service. The services communicate through REST APIs and use JWT tokens for secure authentication.

### Diagram

```plaintext
+----------------+    +------------------+    +-------------------+
| User Service   |<-->| Post Service     |<-->| Comment Service   |
+----------------+    +------------------+    +-------------------+
        ^                   |                        |
        |                   v                        v
   [JWT Authentication]  [Secure REST API]       [Inter-service Comm.]
```

## Containerization with Docker

To ensure seamless deployment and scaling, the services are containerized using Docker. Each service has its own `Dockerfile` that defines how to build and run the service in an isolated environment. Docker Compose is used to orchestrate the services for local development and testing.

### Docker Files
Each service has a `Dockerfile` with the following structure:

1. **Base Image**: Uses an official Java image as the base.
2. **Copy Files**: Copies the compiled JAR file of the service into the container.
3. **Expose Ports**: Exposes the port on which the service runs.
4. **Command to Run the Service**: Executes the JAR file.

Example `Dockerfile` for a service:

```
# Use a base image that supports Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file to the working directory
COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

# Expose the port the application will run on
EXPOSE 9001

# Run the jar file
ENTRYPOINT ["java", "-jar", "user-service.jar"]
```

### Docker Compose
Docker Compose is used to define and run multi-container Docker applications. It links the services together and manages networking.

Example `docker-compose.yml` file:

```
version: '3.8'

services:
  user-service:
    build:
      context: ./user-service
    ports:
      - "9001:9001"
    networks:
      - blog-network
    environment:
      - SPRING_PROFILES_ACTIVE=prod

  post-service:
    build:
      context: ./post-service
    ports:
      - "9002:9002"
    networks:
      - blog-network
    environment:
      - SPRING_PROFILES_ACTIVE=prod

  comment-service:
    build:
      context: ./comment-service
    ports:
      - "9003:9003"
    networks:
      - blog-network
    environment:
      - SPRING_PROFILES_ACTIVE=prod

  api-gateway:
    build:
      context: ./api-gateway
    ports:
      - "9000:9000"
    networks:
      - blog-network
    environment:
      - SPRING_PROFILES_ACTIVE=prod

networks:
  blog-network:
    driver: bridge
```

### How to Run
1. Install Docker and Docker Compose on your machine.
2. Clone this repository and navigate to its root directory.
3. Build the services and run the containers using Docker Compose:

```bash
docker-compose up --build
```
4. Access each service using the exposed ports:
- User Service: `http://localhost:9001`
- Post Service: `http://localhost:9002`
- Comment Service: `http://localhost:9003`
  
### Advantages of Containerization
- **Consistency**: Containers ensure that the application runs the same way in all environments (development, testing, production).
- **Scalability**: Each service can be scaled independently by running additional containers.
- **Isolation**: Each service runs in its own container, avoiding dependency conflicts.
- **Simplified Deployment**: Docker Compose simplifies managing multiple services during development and testing.
