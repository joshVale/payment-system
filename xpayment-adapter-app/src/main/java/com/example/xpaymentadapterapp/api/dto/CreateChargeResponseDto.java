package com.example.xpaymentadapterapp.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a response from X Payment Provider for charge operations.
 */
public record CreateChargeResponseDto(
        UUID id,
        BigDecimal amount,
        String currency,
        BigDecimal amountReceived,
        Instant createdAt,
        Instant chargedAt,
        String customer,
        UUID order,
        String receiptEmail,
        String status,
        Map<String, Object> metadata
) {

    public CreateChargeResponseDto {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}

