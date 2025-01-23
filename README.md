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
