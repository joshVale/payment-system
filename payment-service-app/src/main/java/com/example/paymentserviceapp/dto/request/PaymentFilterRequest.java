package com.example.paymentserviceapp.dto.request;

import com.example.paymentserviceapp.persistence.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFilterRequest(
    String currency,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    Instant createdAfter,
    Instant createdBefore,
    PaymentStatus status
) { }
