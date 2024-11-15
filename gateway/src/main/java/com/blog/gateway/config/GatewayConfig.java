package com.blog.gateway.config;

import com.blog.gateway.filters.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("post_service", r -> r
                        .path("/api/posts/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:9002"))

                .route("comment_service", r -> r
                        .path("/api/comments/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:9003"))
                .build();


    }
}
