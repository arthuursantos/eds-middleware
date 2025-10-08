package com.vilt.eds_middleware.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @GetMapping("/findById")
    public ResponseEntity<String> findById() {
        return ResponseEntity.ok("user info");
    }

}
