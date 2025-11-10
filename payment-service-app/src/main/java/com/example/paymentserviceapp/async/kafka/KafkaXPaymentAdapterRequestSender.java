package com.example.paymentserviceapp.async.kafka;

import com.example.paymentserviceapp.async.AsyncSender;
import com.example.paymentserviceapp.async.XPaymentAdapterRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaXPaymentAdapterRequestSender implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final KafkaTemplate<String, XPaymentAdapterRequestMessage> template;
    private final String topic;

    public KafkaXPaymentAdapterRequestSender(
            KafkaTemplate<String, XPaymentAdapterRequestMessage> template,
            @Value("${app.kafka.topics.xpayment-adapter.request:xpayment-adapter.requests}") String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage msg) {
        String key = msg.paymentGuid().toString();

        log.info("Sending XPayment Adapter request: guid={}, amount={}, currency={} -> topic={}",
                msg.paymentGuid(), msg.amount(), msg.currency(), topic);

        CompletableFuture<SendResult<String, XPaymentAdapterRequestMessage>> future = 
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
