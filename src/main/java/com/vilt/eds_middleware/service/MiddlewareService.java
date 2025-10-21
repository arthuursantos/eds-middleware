package com.vilt.eds_middleware.service;

import com.vilt.eds_middleware.dto.CreateUserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class MiddlewareService {

    private final WebClient serverClient;
    private final WebClient httpClient;
    private final String appName;
    private final String appVersion;
    private final String profile;
    private final String issuerUri;
    private final String serverHealthPath;

    public MiddlewareService(
            WebClient serverClient,
            @Value("${spring.application.name:eds-middleware}") String appName,
            @Value("${application.version:0.0.1}") String appVersion,
            @Value("${spring.profiles.active:default}") String profile,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${eds.server.health-path:/actuator/health}") String serverHealthPath
    ) {
        this.serverClient = serverClient;
        this.httpClient = WebClient.create();
        this.appName = appName;
        this.appVersion = appVersion;
        this.profile = profile;
        this.issuerUri = issuerUri;
        this.serverHealthPath = serverHealthPath;
    }

    public Map<String, Object> health() {
        long started = System.nanoTime();

        Map<String, Object> middleware = Map.of("status", "UP");

        Map<String, Object> edsServer = pingServerHealth();

        Map<String, Object> keycloak = pingKeycloakWellKnown();
        Map<String, Object> keycloakPlus = new LinkedHashMap<>(keycloak);
        keycloakPlus.put("issuer", issuerUri);

        String agg = aggregateStatus(middleware, edsServer, keycloakPlus);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", agg);
        body.put("app", Map.of(
                "name", appName,
                "version", appVersion,
                "profile", profile,
                "uptimeMs", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime(),
                "startupLatencyMs", (System.nanoTime() - started) / 1_000_000
        ));
        body.put("components", Map.of(
                "middleware", middleware,
                "edsServer", edsServer,
                "keycloak", keycloakPlus
        ));
        body.put("timestamp", OffsetDateTime.now().toString());
        return body;
    }

    private Map<String, Object> pingServerHealth() {
        return checkEndpoint(() ->
                serverClient.get()
                        .uri(serverHealthPath)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toBodilessEntity()
                        .block()
        );
    }

    private Map<String, Object> pingKeycloakWellKnown() {
        String wellKnown = issuerUri.endsWith("/")
                ? issuerUri + ".well-known/openid-configuration"
                : issuerUri + "/.well-known/openid-configuration";

        return checkEndpoint(() ->
                httpClient.get()
                        .uri(wellKnown)
                        .retrieve()
                        .toBodilessEntity()
                        .block()
        );
    }

    private Map<String, Object> checkEndpoint(RunnablePing ping) {
        long t0 = System.nanoTime();
        try {
            ping.run();
            long ms = elapsedMs(t0);
            return Map.of("status", "UP", "latencyMs", ms);
        } catch (Exception e) {
            long ms = elapsedMs(t0);
            return Map.of(
                    "status", "DOWN",
                    "latencyMs", ms,
                    "error", e.getClass().getSimpleName(),
                    "message", String.valueOf(e.getMessage())
            );
        }
    }

    private long elapsedMs(long t0) {
        return (System.nanoTime() - t0) / 1_000_000;
    }

    private String aggregateStatus(Map<String, Object>... comps) {
        boolean anyDown = Arrays.stream(comps).anyMatch(m -> "DOWN".equals(m.get("status")));
        if (anyDown) return "DOWN";
        boolean anyWarn = Arrays.stream(comps).anyMatch(m -> "WARN".equals(m.get("status")));
        return anyWarn ? "DEGRADED" : "UP";
    }

    @FunctionalInterface
    private interface RunnablePing {
        void run();
    }

    public Object listUsers() {
        return serverClient.get()
                .uri("/users")
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Object createUser(CreateUserRequest req) {
        return serverClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Map<String, Object> login (String username, String password){
        String tokenEndpoint = issuerUri.endsWith("/") ? issuerUri.substring(0, issuerUri.length() - 1) : issuerUri;
        tokenEndpoint = tokenEndpoint + "/protocol/openid-connect/token";

        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("grant_type", "password");
        formData.put(
                "client_id",
                System.getProperty("security.oauth2.client-id",
                System.getenv().getOrDefault("SECURITY_OAUTH2_CLIENT_ID",
                "eds-middleware")));

        String clientSecret = System.getProperty(
                "security.oauth2.client-secret",
                System.getenv("SECURITY_OAUTH2_CLIENT_SECRET")
        );

        if (clientSecret != null && !clientSecret.isBlank()) {
            formData.put("client_secret", clientSecret);
        }

        formData.put("username", username);
        formData.put("password", password);
        formData.put("scope", "openid profile email");

        var tokenResponse = httpClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData.entrySet().stream()
                        .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                        .reduce((a, b) -> a + "&" + b)
                        .orElse(""))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new IllegalStateException("Token endpoint did not return an access_token");
        }

        Map<String, Object> payload = Map.of(
                "login", username,
                "password", password
        );

        try {
            serverClient.post()
                    .uri("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ignored) { }

        Map<String, Object> out = new LinkedHashMap<>();
        out.putAll(tokenResponse);
        return out;
    }

    private String urlEncode (String v){
        try {
            return java.net.URLEncoder.encode(v, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return v;
        }
    }
}