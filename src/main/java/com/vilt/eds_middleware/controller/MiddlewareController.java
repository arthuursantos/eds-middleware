package com.vilt.eds_middleware.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RestController
@RequestMapping("/middleware")
public class MiddlewareController {

    private final WebClient serverClient;

    public MiddlewareController(@Value("${eds.server.base-url}") String baseUrl) {
        this.serverClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("middleware", "UP"));
    }

    public record CreateUserRequest(@NotBlank String login, @NotBlank String password) {}

    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object createUserViaServer(@RequestBody @Valid CreateUserRequest req) {
        return serverClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object listUsersViaServer() {
        return serverClient.get()
                .uri("/users")
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}
