package com.example.xpaymentadapterapp.async;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record XPaymentAdapterRequestMessage(
        UUID paymentGuid,
        BigDecimal amount,
        String currency,
        UUID messageId,
        Instant occurredAt
) implements Message {
}
