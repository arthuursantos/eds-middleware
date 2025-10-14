package com.vilt.eds_middleware.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps Keycloak "realm_access.roles" (and "resource_access.<client>.roles") into ROLE_* authorities.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String resourceClient;

    public KeycloakRealmRoleConverter() {
        this.resourceClient = null;
    }

    public KeycloakRealmRoleConverter(String resourceClient) {
        this.resourceClient = resourceClient;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object r = realmAccess.get("roles");
            if (r instanceof Collection<?> rc) {
                for (Object o : rc) {
                    roles.add(String.valueOf(o));
                }
            }
        }

        if (resourceClient != null) {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                Object clientBlock = resourceAccess.get(resourceClient);
                if (clientBlock instanceof Map<?, ?> cb) {
                    Object clientRoles = ((Map<?, ?>) cb).get("roles");
                    if (clientRoles instanceof Collection<?> cr) {
                        for (Object o : cr) {
                            roles.add(String.valueOf(o));
                        }
                    }
                }
            }
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
