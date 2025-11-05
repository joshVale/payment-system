package com.example.paymentserviceapp.service.impl;

import com.example.paymentserviceapp.async.XPaymentAdapterRequestMessage;
import com.example.paymentserviceapp.async.event.PaymentRequestEvent;
import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.exception.EntityNotFoundException;
import com.example.paymentserviceapp.mapper.PaymentMapper;
import com.example.paymentserviceapp.mapper.XPaymentAdapterMapper;
import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import com.example.paymentserviceapp.persistency.PaymentFilter;
import com.example.paymentserviceapp.persistency.PaymentFilterFactory;
import com.example.paymentserviceapp.persistency.PaymentRepository;
import com.example.paymentserviceapp.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final XPaymentAdapterMapper xPaymentAdapterMapper;
    private final ApplicationEventPublisher eventPublisher;

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

        log.info("Payment created with PROCESSING status: guid={}, amount={}, currency={}",
            savedPayment.getGuid(), savedPayment.getAmount(), savedPayment.getCurrency());

        // Отправляем событие для асинхронной обработки через X Payment Adapter
        final XPaymentAdapterRequestMessage requestMessage =
            xPaymentAdapterMapper.toXPaymentAdapterRequestMessage(savedPayment);
        eventPublisher.publishEvent(new PaymentRequestEvent(this, requestMessage));
        log.debug("Payment request event published: messageId={}, paymentGuid={}",
            requestMessage.messageId(), requestMessage.paymentGuid());

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

    @Override
    @Transactional
    public void updatePaymentStatus(UUID paymentGuid, UUID transactionRefId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentGuid)
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", paymentGuid);
                    return new EntityNotFoundException("Payment not found", "update-status-op", paymentGuid);
                });

        log.debug("Current payment state: guid={}, status={}, transactionRefId={}",
                payment.getGuid(), payment.getStatus(), payment.getTransactionRefId());

        payment.setTransactionRefId(transactionRefId);
        payment.setStatus(status);

        Payment updated = paymentRepository.save(payment);

        log.info("Payment updated successfully: guid={}, newStatus={}, transactionRefId={}",
                updated.getGuid(), updated.getStatus(), updated.getTransactionRefId());
    }
}
