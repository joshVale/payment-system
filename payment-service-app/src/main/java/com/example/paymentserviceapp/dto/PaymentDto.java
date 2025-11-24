package com.example.paymentserviceapp.dto;

import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record PaymentDto(
    UUID guid,
    UUID inquiryRefId,
    BigDecimal amount,
    String currency,
    UUID transactionRefId,
    PaymentStatus status,
    String note,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) { }
