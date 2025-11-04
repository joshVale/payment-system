package com.example.paymentserviceapp.async;

import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import com.example.paymentserviceapp.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class XPaymentAdapterResponseMessageHandler implements MessageHandler<XPaymentAdapterResponseMessage> {

    private PaymentService paymentService;

    @Autowired
    @Lazy
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void handle(XPaymentAdapterResponseMessage message) {
        log.info("Handling response message: messageGuid={}, paymentGuid={}, status={}, transactionRefId={}",
                message.messageId(), message.paymentGuid(), message.status(), message.transactionRefId());

        PaymentStatus paymentStatus = mapStatus(message.status());
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
