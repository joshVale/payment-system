package com.example.paymentserviceapp.mapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TestConstants {

    public static final UUID PAYMENT_GUID = UUID.fromString("d2d9f8f0-2c63-4f9e-b832-9d312d9473e2");
    public static final UUID INQUIRY_REF_ID = UUID.fromString("a1b2c3d4-1234-5678-9101-abcdef123456");
    public static final UUID TRANSACTION_REF_ID = UUID.fromString("f1e2d3c4-5678-9101-2345-abcdef654321");
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final BigDecimal AMOUNT_1200 = new BigDecimal("1200.50");
    public static final BigDecimal AMOUNT_550 = new BigDecimal("550.75");
    public static final String NOTE_TEST_PAYMENT = "Test payment";
    public static final String NOTE_INVOICE_22 = "Invoice #22";
    public static final OffsetDateTime CREATED_AT = OffsetDateTime.now().minusDays(1);
    public static final OffsetDateTime UPDATED_AT = OffsetDateTime.now();
}