package com.example.xpaymentadapterapp.checkstate;

import java.math.BigDecimal;
import java.util.UUID;
public interface PaymentStateCheckRegistrar {
    void register(

            UUID chargeGuid,
            UUID paymentGuid,
            BigDecimal amount,
            String currency);

}
