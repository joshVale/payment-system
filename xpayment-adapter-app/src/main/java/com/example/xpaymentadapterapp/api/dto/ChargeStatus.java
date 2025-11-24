package com.example.xpaymentadapterapp.api.dto;

import java.util.Map;
import java.util.Set;

/**
 * Enum для статусов платежей в X Payment Provider.
 */
public enum ChargeStatus {
    SUCCEEDED,
    CANCELED,
    CANCELLED,
    PROCESSING,
    PENDING,
    FAILED;
    
    /**
     * Множество терминальных статусов (платеж завершен).
     */
    public static final Set<ChargeStatus> TERMINAL_STATUSES = Set.of(
            SUCCEEDED,
            CANCELED,
            CANCELLED,
            FAILED
    );
    
    /**
     * Map для быстрой проверки терминальных статусов по строковому значению.
     */
    public static final Map<String, ChargeStatus> STATUS_MAP = Map.of(
            "succeeded", SUCCEEDED,
            "canceled", CANCELED,
            "cancelled", CANCELLED,
            "processing", PROCESSING,
            "pending", PENDING,
            "failed", FAILED
    );
    
    /**
     * Проверяет, является ли статус терминальным.
     */
    public static boolean isTerminal(String status) {
        if (status == null) {
            return false;
        }
        ChargeStatus chargeStatus = STATUS_MAP.get(status.toLowerCase());
        return chargeStatus != null && TERMINAL_STATUSES.contains(chargeStatus);
    }
    
    /**
     * Преобразует строковый статус в enum.
     */
    public static ChargeStatus fromString(String status) {
        if (status == null) {
            return PROCESSING;
        }
        return STATUS_MAP.getOrDefault(status.toLowerCase(), PROCESSING);
    }
}

