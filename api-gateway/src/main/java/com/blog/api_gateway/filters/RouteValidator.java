package com.blog.api_gateway.filters;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/authenticate",
            "/api/v1/auth/refresh-token"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        boolean isSecured = openApiEndpoints
                .stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
        Logger logger = LoggerFactory.getLogger(RouteValidator.class);
        if (isSecured) {
            logger.info("Request to {} is secured", request.getURI().getPath());
        } else {
            logger.info("Request to {} is not secured", request.getURI().getPath());
        }
        return isSecured;
    };
}
