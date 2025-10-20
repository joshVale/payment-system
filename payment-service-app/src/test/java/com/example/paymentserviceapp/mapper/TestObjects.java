package com.example.paymentserviceapp.mapper;

import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.persistence.entity.PaymentStatus;

import static com.example.paymentserviceapp.mapper.TestConstants.*;

public class TestObjects {

    public static Payment createPaymentEntity() {
        return Payment.builder()
                .guid(PAYMENT_GUID)
                .inquiryRefId(INQUIRY_REF_ID)
                .amount(AMOUNT_1200)
                .currency(CURRENCY_USD)
                .transactionRefId(TRANSACTION_REF_ID)
                .status(PaymentStatus.CREATED)
                .note(NOTE_TEST_PAYMENT)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    public static PaymentDto createPaymentDto() {
        return PaymentDto.builder()
                .guid(PAYMENT_GUID)
                .inquiryRefId(INQUIRY_REF_ID)
                .amount(AMOUNT_550)
                .currency(CURRENCY_EUR)
                .transactionRefId(TRANSACTION_REF_ID)
                .status(PaymentStatus.PENDING)
                .note(NOTE_INVOICE_22)
                .createdAt(CREATED_AT.minusDays(1))
                .updatedAt(UPDATED_AT)
                .build();
    }
}
