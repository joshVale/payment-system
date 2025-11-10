package com.example.paymentserviceapp.async.kafka;

import com.example.paymentserviceapp.async.AsyncListener;
import com.example.paymentserviceapp.async.MessageHandler;
import com.example.paymentserviceapp.async.XPaymentAdapterResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaXPaymentAdapterResultListenerAdapter implements AsyncListener<XPaymentAdapterResponseMessage> {

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

    @Override
    public void onMessage(XPaymentAdapterResponseMessage message) {
        handler.handle(message);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.xpayment-adapter.response}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            XPaymentAdapterResponseMessage message,
            ConsumerRecord<String, XPaymentAdapterResponseMessage> record,
            Acknowledgment ack
    ) {
        try {
            log.info("📩 Received XPayment Adapter response: paymentGuid={}, status={}, partition={}, offset={}",
                    message.paymentGuid(), message.status(), record.partition(), record.offset());

            onMessage(message);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("❌ Error handling XPayment Adapter response for paymentGuid={}", message.paymentGuid(), e);
            throw e;
        }
    }
}
