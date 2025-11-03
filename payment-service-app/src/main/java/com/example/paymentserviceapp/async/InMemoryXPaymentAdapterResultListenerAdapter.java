package com.example.paymentserviceapp.async;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InMemoryXPaymentAdapterResultListenerAdapter
        implements AsyncListener<XPaymentAdapterResponseMessage> {

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

    @Override
    public void onMessage(XPaymentAdapterResponseMessage msg) {
        log.info("Listener received message: messageId={}, paymentGuid={}, status={}, transactionRefId={}",
                msg.getMessageId(), msg.getPaymentGuid(), msg.getStatus(), msg.getTransactionRefId());

        try {
            handler.handle(msg);
            log.info("Message handled successfully: messageId={}, paymentGuid={}",
                    msg.getMessageId(), msg.getPaymentGuid());
        } catch (Exception e) {
            log.error("Error handling message: messageId={}, paymentGuid={}, error={}",
                    msg.getMessageId(), msg.getPaymentGuid(), e.getMessage(), e);
            throw e;
        }
    }
}
