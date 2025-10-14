package com.vilt.eds_middleware.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank
        String login,
        @NotBlank
        String password
) { }
