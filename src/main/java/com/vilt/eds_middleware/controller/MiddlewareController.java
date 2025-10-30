package com.vilt.eds_middleware.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/middleware")
public class MiddlewareController {

    private final WebClient serverClient;

    public MiddlewareController(@Value("${eds.server.base-url}") String baseUrl) {
        this.serverClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public record UserLoginDTO(@NotBlank String username, @NotBlank String password) {}

    public record UserDTO(@NotBlank String emailId,
                          @NotBlank String username,
                          @NotBlank String password,
                          @NotBlank String firstName,
                          @NotBlank String lastName) {
    }


    @PostMapping(path = "/user/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object createUserViaServer(@RequestBody @Valid UserDTO req) {
        return serverClient.post()
                .uri("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    @PostMapping(path = "/user/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object loginViaServer(@RequestBody @Valid UserLoginDTO req) {
        return serverClient.post()
                .uri("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    @GetMapping(path = "/user/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object testViaServer(@RequestHeader("Authorization") String bearerToken) {
        return serverClient.get()
                .uri("/api/user/test")
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
