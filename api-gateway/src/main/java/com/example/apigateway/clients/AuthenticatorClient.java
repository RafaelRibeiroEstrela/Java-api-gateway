package com.example.apigateway.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthenticatorClient {

    private final WebClient.Builder webClientBuilder;

    public AuthenticatorClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Boolean> isAllowed(String method, String path, String token) {
        return webClientBuilder.build().get()
                .uri("lb://bff/v1/auth/is-allowed")
                .header("token", token)
                .header("path", path)
                .header("method", method)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
