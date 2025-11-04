package com.example.paymentserviceapp.async;

import com.example.paymentserviceapp.exception.AsyncMessageProcessingException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InMemoryXPaymentAdapterResultListenerAdapter
        implements AsyncListener<XPaymentAdapterResponseMessage> {

    @Lazy
    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

    @Override
    public void onMessage(XPaymentAdapterResponseMessage msg) {
        log.info("Listener received message: messageId={}, paymentGuid={}, status={}, transactionRefId={}",
                msg.messageId(), msg.paymentGuid(), msg.status(), msg.transactionRefId());

        try {
            handler.handle(msg);
            log.info("Message handled successfully: messageId={}, paymentGuid={}",
                    msg.messageId(), msg.paymentGuid());
        } catch (Exception e) {
            log.error("Error handling message: messageId={}, paymentGuid={}, error={}",
                    msg.messageId(), msg.paymentGuid(), e.getMessage(), e);
            throw new AsyncMessageProcessingException("Ошибка обработки сообщения", e);
        }
    }
}
