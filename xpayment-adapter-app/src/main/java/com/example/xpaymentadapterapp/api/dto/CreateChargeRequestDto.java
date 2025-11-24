package com.example.xpaymentadapterapp.api.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a request to create a charge in X Payment Provider.
 */
public record CreateChargeRequestDto(
        BigDecimal amount,
        String currency,
        String customer,
        UUID order,
        String receiptEmail,
        Map<String, Object> metadata
) {

    public CreateChargeRequestDto {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}

