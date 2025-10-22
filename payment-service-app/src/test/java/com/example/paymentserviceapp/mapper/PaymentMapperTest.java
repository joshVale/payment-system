package com.example.paymentserviceapp.mapper;


import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.persistence.entity.Payment;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.example.paymentserviceapp.mapper.TestObjects.createPaymentDto;
import static com.example.paymentserviceapp.mapper.TestObjects.createPaymentEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class PaymentMapperTest {

    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void shouldMapPaymentToPaymentDto() {
        Payment payment = createPaymentEntity();

        PaymentDto dto = paymentMapper.toPaymentDto(payment);

        assertNotNull(dto);
        assertEquals(payment.getGuid(), dto.guid());
        assertEquals(payment.getAmount(), dto.amount());
        assertEquals(payment.getCurrency(), dto.currency());
        assertEquals(payment.getStatus(), dto.status());
    }

    @Test
    void shouldMapPaymentDtoToPaymentEntity() {
        PaymentDto dto = createPaymentDto();

        Payment payment = paymentMapper.toPaymentEntity(dto);

        assertNotNull(payment);
        assertEquals(dto.guid(), payment.getGuid());
        assertEquals(dto.amount(), payment.getAmount());
        assertEquals(dto.currency(), payment.getCurrency());
        assertEquals(dto.status(), payment.getStatus());
    }
}