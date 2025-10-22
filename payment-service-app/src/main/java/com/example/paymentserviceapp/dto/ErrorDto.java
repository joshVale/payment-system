package com.example.paymentserviceapp.dto;

import java.time.Instant;
import java.util.UUID;

public record ErrorDto(
        UUID id,
        String operation,
        String errorMessage,
        Instant timestamp
) {
    public ErrorDto(UUID id, String operation, String errorMessage) {
        this(id, operation, errorMessage, Instant.now());
    }
}
