package com.example.apigateway.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthenticatorClient {

    private final WebClient webClient;

    public AuthenticatorClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Boolean> isPublic(String path) {
        return webClient.get()
                .uri("lb://ms-security/v1/auth/is-public-path")
                .header("path", path)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> validate(String token) {
        return webClient.get()
                .uri("lb://ms-security/v1/auth/validate-token")
                .header("token", token)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
