package com.vilt.eds_middleware.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/middleware")
public class MiddlewareController {

    @GetMapping
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("vai corinthians");
    }
}
