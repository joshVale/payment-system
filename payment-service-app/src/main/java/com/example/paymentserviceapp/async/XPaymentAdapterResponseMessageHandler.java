package com.example.paymentserviceapp.async;


import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import com.example.paymentserviceapp.persistency.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class XPaymentAdapterResponseMessageHandler implements MessageHandler<XPaymentAdapterResponseMessage> {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void handle(XPaymentAdapterResponseMessage message) {
        log.info("Handling response message: messageGuid={}, paymentGuid={}, status={}, transactionRefId={}",
                message.getMessageId(), message.getPaymentGuid(), message.getStatus(), message.getTransactionRefId());

        Payment payment = paymentRepository.findById(message.getPaymentGuid())
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", message.getPaymentGuid());
                    return new IllegalArgumentException("Payment not found: " + message.getPaymentGuid());
                });

        log.debug("Current payment state: guid={}, status={}, transactionRefId={}",
                payment.getGuid(), payment.getStatus(), payment.getTransactionRefId());

        // Обновляем платеж на основе статуса адаптера
        payment.setTransactionRefId(message.getTransactionRefId());
        payment.setStatus(mapStatus(message.getStatus()));

        Payment updated = paymentRepository.save(payment);

        log.info("Payment updated successfully: guid={}, newStatus={}, transactionRefId={}",
                updated.getGuid(), updated.getStatus(), updated.getTransactionRefId());
    }

    private PaymentStatus mapStatus(XPaymentAdapterStatus adapterStatus) {
        return switch (adapterStatus) {
            case PROCESSING -> PaymentStatus.PROCESSING;
            case SUCCEEDED -> PaymentStatus.SUCCEEDED;
            case CANCELED -> PaymentStatus.CANCELED;
        };
    }
}
