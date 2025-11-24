package com.example.xpaymentadapterapp.checkstate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMQProperties(
        String exchangeName,
        String queueName,
        String delayedExchangeName,
        String dlxExchangeName,
        String dlxRoutingKey,
        int maxAttempts,
        int maxRetries,
        long intervalMs
) {
}

