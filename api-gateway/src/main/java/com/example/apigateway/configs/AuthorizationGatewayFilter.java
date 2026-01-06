package com.example.apigateway.configs;

import com.example.apigateway.clients.AuthenticatorClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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

        return authenticatorClient.isPublic(path)
                .defaultIfEmpty(false)
                .flatMap(isPublic -> {
                    // SE SIM, PERMITIR
                    if (Boolean.TRUE.equals(isPublic)) {
                        return chain.filter(exchange);
                    }

                    // SE NAO, OBTER TOKEN E VALIDAR TOKEN
                    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                        return deny(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    String token = authHeader.substring(BEARER_PREFIX.length()).trim();
                    if (token.isEmpty()) {
                        return deny(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    return authenticatorClient.validate(token)
                            .defaultIfEmpty(false)
                            .flatMap(isValidToken -> {
                                if (Boolean.TRUE.equals(isValidToken)) {
                                    return chain.filter(exchange);
                                }
                                return deny(exchange, HttpStatus.UNAUTHORIZED);
                            })
                            // Se a Auth API falhar, por segurança: negar (ou mude para 503 se preferir)
                            .onErrorResume(e -> deny(exchange, HttpStatus.UNAUTHORIZED));
                })
                // Se isPublic der erro, por segurança negar (ou 503)
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
