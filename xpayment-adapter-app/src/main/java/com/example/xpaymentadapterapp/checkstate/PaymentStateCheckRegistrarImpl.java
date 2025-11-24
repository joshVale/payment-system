package com.example.xpaymentadapterapp.checkstate;

import com.example.xpaymentadapterapp.checkstate.config.RabbitMQConstants;
import com.example.xpaymentadapterapp.checkstate.config.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentStateCheckRegistrarImpl implements PaymentStateCheckRegistrar {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    @Override
    public void register(
            UUID chargeGuid,
            UUID paymentGuid,
            BigDecimal amount,
            String currency
    ) {
        PaymentCheckStateMessage message = new PaymentCheckStateMessage(
                chargeGuid,
                paymentGuid,
                amount,
                currency
        );
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.exchangeName(),
                rabbitMQProperties.queueName(),
                message,
                m -> {
                    m.getMessageProperties().setHeader(RabbitMQConstants.X_DELAY_HEADER,
                            rabbitMQProperties.intervalMs());
                    m.getMessageProperties().setHeader(RabbitMQConstants.X_RETRY_COUNT_HEADER, 1);
                    return m;
                }
        );
    }
}
