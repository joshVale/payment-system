package com.example.paymentserviceapp.dto.request;


import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    UUID inquiryRefId,
    BigDecimal amount,
    String currency,
    String note
) { }



