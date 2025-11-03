package com.example.paymentserviceapp.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    @NotNull(message = "Inquiry reference ID is required")
    UUID inquiryRefId,
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    String currency,
    
    String note
) { }



