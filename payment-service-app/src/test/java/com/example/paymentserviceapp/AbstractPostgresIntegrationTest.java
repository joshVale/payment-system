package com.example.paymentserviceapp;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

/**
 * Абстрактный класс для интеграционных тестов с использованием Testcontainers.
 * Поднимает контейнер PostgreSQL и подставляет настройки БД в Spring Context.
 * Все интеграционные тесты должны наследоваться от этого класса.
 */
@SpringBootTest
@Testcontainers
@Transactional
public abstract class AbstractPostgresIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("payment-db")
            .withUsername("test")
            .withPassword("test")
            // Ожидание, пока контейнер действительно готов к работе
            .waitingFor(new WaitAllStrategy()
                    .withStrategy(Wait.forListeningPort())
                    .withStrategy(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
            )
            .withStartupTimeout(Duration.ofSeconds(200));

    /**
     * Переопределяет свойства datasource для использования контейнера PostgreSQL.
     * Указывает отдельный changelog для тестовой базы.
     */
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.liquibase.change-log", () ->
                "classpath:/db/master-test-changelog.yaml"
        );
        // Отключаем валидацию схемы для тестов
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.show-sql", () -> "false");
    }
}