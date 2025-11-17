package com.example.xpaymentadapterapp.async.kafka;

import com.example.xpaymentadapterapp.async.AsyncSender;
import com.example.xpaymentadapterapp.async.XPaymentAdapterResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaXPaymentAdapterRequestSender implements AsyncSender<XPaymentAdapterResponseMessage> {

    private final KafkaTemplate<String, XPaymentAdapterResponseMessage> template;
    
    @Value("${app.kafka.topics.x-payment-adapter.response:xpayment-adapter.responses}")
    private String topic;

    @Override
    public void send(XPaymentAdapterResponseMessage msg) {
        String key = msg.paymentGuid().toString();

        log.info("Sending XPayment Adapter response: guid={}, amount={}, currency={}, status={} -> topic={}",
                msg.paymentGuid(), msg.amount(), msg.currency(), msg.status(), topic);

        CompletableFuture<SendResult<String, XPaymentAdapterResponseMessage>> future =
                template.send(topic, key, msg);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                if (result != null) {
                    log.debug("Successfully sent message to topic {} partition {} offset {}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            } else {
                log.error("Failed to send message to topic {}: guid={}, error={}",
                        topic, msg.paymentGuid(), ex.getMessage(), ex);
            }
        });
    }
}
