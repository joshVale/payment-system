package com.example.xpaymentadapterapp.async.kafka;

import com.example.xpaymentadapterapp.async.AsyncListener;
import com.example.xpaymentadapterapp.async.MessageHandler;
import com.example.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaXPaymentAdapterResultListenerAdapter implements AsyncListener<XPaymentAdapterRequestMessage> {

    private final MessageHandler<XPaymentAdapterRequestMessage> handler;

    @Override
    public void onMessage(XPaymentAdapterRequestMessage message) {
        handler.handle(message);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.x-payment-adapter.request}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            XPaymentAdapterRequestMessage message,
            ConsumerRecord<String, XPaymentAdapterRequestMessage> record,
            Acknowledgment ack
    ) {
        try {
            log.info("📩 Received XPayment Adapter request: paymentGuid={}, amount={}, currency={}, partition={}, offset={}",
                    message.paymentGuid(), message.amount(), message.currency(), record.partition(), record.offset());

            onMessage(message);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("❌ Error handling XPayment Adapter request for paymentGuid={}", message.paymentGuid(), e);
            throw e;
        }
    }
}
