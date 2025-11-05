package com.example.paymentserviceapp.async;

import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import com.example.paymentserviceapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class XPaymentAdapterResponseMessageHandler implements MessageHandler<XPaymentAdapterResponseMessage> {

    private final PaymentService paymentService;

    @Override
    public void handle(XPaymentAdapterResponseMessage message) {
        log.info("Handling response message: messageGuid={}, paymentGuid={}, status={}, transactionRefId={}",
            message.messageId(), message.paymentGuid(), message.status(), message.transactionRefId());

        final PaymentStatus paymentStatus = mapStatus(message.status());
        paymentService.updatePaymentStatus(message.paymentGuid(), message.transactionRefId(), paymentStatus);
    }

    private PaymentStatus mapStatus(XPaymentAdapterStatus adapterStatus) {
        return switch (adapterStatus) {
            case PROCESSING -> PaymentStatus.PROCESSING;
            case SUCCEEDED -> PaymentStatus.SUCCEEDED;
            case CANCELED -> PaymentStatus.CANCELED;
        };
    }
}
