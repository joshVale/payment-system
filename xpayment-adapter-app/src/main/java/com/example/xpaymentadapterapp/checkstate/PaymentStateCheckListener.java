package com.example.xpaymentadapterapp.checkstate;

import com.example.xpaymentadapterapp.checkstate.config.RabbitMQConstants;
import com.example.xpaymentadapterapp.checkstate.config.RabbitMQProperties;
import com.example.xpaymentadapterapp.checkstate.handler.PaymentStatusCheckHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStateCheckListener {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;
    private final PaymentStatusCheckHandler paymentStatusCheckHandler;

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void handle(PaymentCheckStateMessage message, Message raw) {
        MessageProperties props = raw.getMessageProperties();
        int retryCount = (int) props.getHeaders().getOrDefault(RabbitMQConstants.X_RETRY_COUNT_HEADER, 0);
        
        boolean completed = paymentStatusCheckHandler.handle(message.chargeGuid());
        if (completed) {
            log.info("Payment status check completed for chargeGuid: {}, paymentGuid: {}", 
                    message.chargeGuid(), message.paymentGuid());
            // Уведомление payment-service уже отправлено через PaymentCompletionNotifier в PaymentStatusCheckHandler
            return;
        }
        
        if (retryCount < rabbitMQProperties.maxRetries()) {
            // Планируем следующую проверку
            PaymentCheckStateMessage newMessage = new PaymentCheckStateMessage(
                    message.chargeGuid(),
                    message.paymentGuid(),
                    message.amount(),
                    message.currency()
            );
            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.exchangeName(),
                    rabbitMQProperties.queueName(),
                    newMessage,
                    m -> {
                        m.getMessageProperties().setHeader(RabbitMQConstants.X_DELAY_HEADER, 
                                rabbitMQProperties.intervalMs());
                        m.getMessageProperties().setHeader(RabbitMQConstants.X_RETRY_COUNT_HEADER, retryCount + 1);
                        return m;
                    }
            );
            log.debug("Scheduled next status check for chargeGuid: {}, retryCount: {}", 
                    message.chargeGuid(), retryCount + 1);
        } else {
            // Исчерпали попытки -- кладём сообщение в DLX
            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.dlxExchangeName(),
                    rabbitMQProperties.dlxRoutingKey(),
                    message,
                    m -> {
                        m.getMessageProperties().setHeader(RabbitMQConstants.X_RETRY_COUNT_HEADER, retryCount);
                        m.getMessageProperties().setHeader(RabbitMQConstants.X_FINAL_STATUS_HEADER, "TIMEOUT");
                        m.getMessageProperties().setHeader(RabbitMQConstants.X_ORIGINAL_QUEUE_HEADER, 
                                rabbitMQProperties.queueName());
                        return m;
                    }
            );
            log.warn("Max retries exceeded for chargeGuid: {}, paymentGuid: {}, sending to DLX", 
                    message.chargeGuid(), message.paymentGuid());
        }
    }
}
