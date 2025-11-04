package com.example.paymentserviceapp.async;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record XPaymentAdapterRequestMessage(
        UUID paymentGuid,
        BigDecimal amount,
        String currency,
        UUID messageId,
        OffsetDateTime occurredAt
) implements Message {
}
