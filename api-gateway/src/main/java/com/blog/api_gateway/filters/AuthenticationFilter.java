package com.blog.api_gateway.filters;

import com.blog.api_gateway.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            logger.info("Processing request: {}", request.getPath());

            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    logger.warn("Missing authorization header for request: {}", request.getPath());
                    return this.onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        jwtUtil.validateToken(token);
                        logger.info("Token validated successfully for request: {}", request.getPath());

                        // Forward the token to the downstream service
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header(HttpHeaders.AUTHORIZATION, authHeader)
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } catch (ExpiredJwtException e) {
                        logger.error("Token has expired for request: {}", request.getPath(), e);
                        return this.onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
                    } catch (MalformedJwtException e) {
                        logger.error("Malformed JWT token for request: {}", request.getPath(), e);
                        return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    } catch (SignatureException e) {
                        logger.error("Invalid JWT token signature for request: {}", request.getPath(), e);
                        return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    } catch (RuntimeException e) {
                        logger.error("Unexpected error during token validation for request: {}", request.getPath(), e);
                        return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    }
                } else {
                    logger.warn("Invalid Authorization header for request: {}", request.getPath());
                    return this.onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.error(err);
        return response.setComplete();
    }

    public static class Config {
    }
}
