package com.example.paymentserviceapp.service.impl;

import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.exception.EntityNotFoundException;
import com.example.paymentserviceapp.mapper.PaymentMapper;
import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.persistency.PaymentFilter;
import com.example.paymentserviceapp.persistency.PaymentFilterFactory;
import com.example.paymentserviceapp.persistency.PaymentRepository;
import com.example.paymentserviceapp.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toPaymentDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toPaymentDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Платеж не найден", "find-by-id-op", id)
                );
    }

    @Override
    public Page<PaymentDto> searchPaged(PaymentFilter paymentFilter, Pageable pageable) {
        final Specification<Payment> spec = PaymentFilterFactory.fromFilter(paymentFilter);
        return paymentRepository.findAll(spec, pageable)
            .map(paymentMapper::toPaymentDto);
    }

    @Override
    public PaymentDto createPayment(PaymentDto paymentDto) {
        final Payment payment = paymentMapper.toPaymentEntity(paymentDto);
        final Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto updatePayment(UUID id, PaymentDto dto) {
        final Payment payment = paymentRepository.findByGuidForUpdate(id)
            .orElseThrow(() -> new EntityNotFoundException("Платеж не найден", "update-op", id));
        paymentMapper.updatePaymentFromDto(dto, payment);
        final Payment saved = paymentRepository.save(payment);

        return paymentMapper.toPaymentDto(saved);
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw new EntityNotFoundException("Платеж не найден", "delete-op", id);
        }
        paymentRepository.deleteById(id);
    }
}
