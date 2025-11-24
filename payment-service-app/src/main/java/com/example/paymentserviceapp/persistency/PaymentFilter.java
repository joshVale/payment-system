package com.example.paymentserviceapp.persistency;

import com.example.paymentserviceapp.persistence.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;


public record PaymentFilter(
    String currency,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    Instant createdAfter,
    Instant createdBefore,
    PaymentStatus status
) { }
