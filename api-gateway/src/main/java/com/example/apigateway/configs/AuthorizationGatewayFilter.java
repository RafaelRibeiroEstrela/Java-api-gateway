package com.example.apigateway.configs;

import com.example.apigateway.clients.AuthenticatorClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationGatewayFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticatorClient authenticatorClient;

    public AuthorizationGatewayFilter(AuthenticatorClient authenticatorClient) {
        this.authenticatorClient = authenticatorClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String token = null;
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            token = authHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return authenticatorClient.isAllowed(path, token)
                .flatMap(isAllowed -> {
                    if (Boolean.TRUE.equals(isAllowed)) {
                        return chain.filter(exchange);
                    } else {
                        return deny(exchange, HttpStatus.UNAUTHORIZED);
                    }
                })
                .onErrorResume(e -> deny(exchange, HttpStatus.UNAUTHORIZED));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
