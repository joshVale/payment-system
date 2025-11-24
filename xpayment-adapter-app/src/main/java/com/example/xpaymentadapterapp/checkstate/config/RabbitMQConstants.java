package com.example.xpaymentadapterapp.checkstate.config;

/**
 * Константы для заголовков RabbitMQ сообщений.
 */
public final class RabbitMQConstants {
    
    public static final String X_DELAY_HEADER = "x-delay";
    public static final String X_RETRY_COUNT_HEADER = "x-retry-count";
    public static final String X_FINAL_STATUS_HEADER = "x-final-status";
    public static final String X_ORIGINAL_QUEUE_HEADER = "x-original-queue";
    
    private RabbitMQConstants() {
        // Utility class
    }
}

