package com.vilt.eds_middleware.controller;

import com.vilt.eds_middleware.dto.CreateUserRequest;
import com.vilt.eds_middleware.service.MiddlewareService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/middleware")
public class MiddlewareController {

    private final MiddlewareService service;

    public MiddlewareController(
            MiddlewareService service
    ) {
        this.service = service;
    }

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(service.health());
    }

    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUserViaServer(@RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(service.createUser(req));
    }

    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listUsersViaServer() {
        return ResponseEntity.ok(service.listUsers());
    }

    @PostMapping(
            path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login (@RequestBody com.vilt.eds_middleware.dto.LoginRequest req) {
        Map<String, Object> tokens = service.login(req.username(), req.password());
        return ResponseEntity.ok(tokens);
    }
}