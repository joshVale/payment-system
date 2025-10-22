package com.example.paymentserviceapp.service;

import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.persistency.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<PaymentDto> getAllPayments();

    PaymentDto getPaymentById(UUID guid);

    Page<PaymentDto> searchPaged(PaymentFilter paymentFilter, Pageable pageable);

    PaymentDto createPayment(PaymentDto paymentDto);

    PaymentDto updatePayment(UUID guid, PaymentDto paymentDto);

    void delete(UUID guid);

}
