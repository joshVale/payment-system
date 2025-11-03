package com.example.paymentserviceapp;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Утилитный класс для создания JWT токенов в интеграционных тестах.
 * Используется для подстановки "тестовых пользователей" с нужными ролями.
 */
public class TestJwtFactory {

    /**
     * Создает тестовый JWT токен с заданным пользователем и ролями.
     *
     * @param username имя пользователя (preferred_username)
     * @param roles    список ролей (без префикса ROLE_)
     * @return RequestPostProcessor для MockMvc
     */
    public static RequestPostProcessor jwtWithRole(String username, String... roles) {
        return jwt()
                .jwt(jwt -> {
                    jwt.claim("sub", "123");
                    jwt.claim("preferred_username", username);
                    jwt.claim("realm_access", Map.of("roles", List.of(roles)));
                })
                .authorities(Stream.of(roles)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()));
    }
}
