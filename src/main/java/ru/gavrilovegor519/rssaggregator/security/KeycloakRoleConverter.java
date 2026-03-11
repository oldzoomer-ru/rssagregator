package ru.gavrilovegor519.rssaggregator.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Custom converter to extract roles from Keycloak JWT tokens.
 * Keycloak stores roles in realm_access.roles and resource_access.clientId.roles.
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Extract realm-level roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles") &&
                realmAccess.get("roles") instanceof List<?> realmRoles) {
            realmRoles.stream()
                    .filter(role -> !role.equals("offline_access") && !role.equals("uma_authorization"))
                    .map(role -> {
                        if (role instanceof String roleString) {
                            return new SimpleGrantedAuthority("ROLE_" + roleString.toUpperCase());
                        } else {
                            return new SimpleGrantedAuthority("ROLE_NOBODY");
                        }
                    })
                    .forEach(authorities::add);
        }

        // Extract client-level roles (specific to this client)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().stream()
                    .filter(Map.class::isInstance)
                    .flatMap(v -> {
                        Map<?, ?> clientRoles = (Map<?, ?>) v;
                        if (clientRoles.get("roles") instanceof List<?> roles) {
                            return roles.stream();
                        } else {
                            return Stream.of();
                        }
                    })
                    .map(role -> {
                        if (role instanceof String roleString) {
                            return new SimpleGrantedAuthority("ROLE_" + roleString.toUpperCase());
                        } else {
                            return new SimpleGrantedAuthority("ROLE_NOBODY");
                        }
                    })
                    .forEach(authorities::add);
        }

        return authorities;
    }
}