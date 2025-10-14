package com.vilt.eds_middleware.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class MeController {

    @GetMapping("/user/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sub", jwt.getSubject());
        out.put("name", jwt.getClaimAsString("name"));
        out.put("preferred_username", jwt.getClaimAsString("preferred_username"));
        out.put("email", jwt.getClaimAsString("email"));
        out.put("realm_roles", Optional.ofNullable((Map<String, Object>) jwt.getClaim("realm_access"))
                .map(m -> (List<String>) m.get("roles"))
                .orElseGet(List::of));
        out.put("issued_at", jwt.getIssuedAt());
        out.put("expires_at", jwt.getExpiresAt());
        return out;
    }
}
