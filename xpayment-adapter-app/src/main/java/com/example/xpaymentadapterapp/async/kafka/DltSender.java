package com.example.xpaymentadapterapp.async.kafka;

import com.example.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class DltSender {

    private final KafkaTemplate<String, XPaymentAdapterRequestMessage> template;
    
    @Value("${app.kafka.topics.x-payment-adapter.dlt:xpayment-adapter.requests.DLT}")
    private String dltTopic;

    /**
     * Sends an invalid message to the Dead Letter Topic.
     *
     * @param message the invalid message to send to DLT
     * @param errorMessage the reason why the message is invalid
     */
    public void sendToDlt(XPaymentAdapterRequestMessage message, String errorMessage) {
        String key = message.paymentGuid() != null ? message.paymentGuid().toString() : "unknown";

        log.warn("Sending invalid message to DLT: paymentGuid={}, error={} -> topic={}",
                message.paymentGuid(), errorMessage, dltTopic);

        CompletableFuture<SendResult<String, XPaymentAdapterRequestMessage>> future =
                template.send(dltTopic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                if (result != null) {
                    log.info("Successfully sent invalid message to DLT topic {} partition {} offset {}",
                            dltTopic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            } else {
                log.error("Failed to send invalid message to DLT topic {}: paymentGuid={}, error={}",
                        dltTopic, message.paymentGuid(), ex.getMessage(), ex);
            }
        });
    }
}

