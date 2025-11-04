package com.example.paymentserviceapp.async;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record XPaymentAdapterResponseMessage(
        UUID paymentGuid,
        UUID messageId,
        BigDecimal amount,
        String currency,
        UUID transactionRefId,
        XPaymentAdapterStatus status,
        OffsetDateTime occurredAt
) implements Message {
}
