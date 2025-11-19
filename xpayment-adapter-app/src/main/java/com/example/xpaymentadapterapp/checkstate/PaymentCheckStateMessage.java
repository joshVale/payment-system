package com.example.xpaymentadapterapp.checkstate;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCheckStateMessage(
        UUID chargeGuid,
        UUID paymentGuid,
        BigDecimal amount,
        String currency
) {
}
