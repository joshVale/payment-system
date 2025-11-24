package com.example.xpaymentadapterapp.checkstate;

import com.example.xpaymentadapterapp.checkstate.config.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitMqPaymentRetryConfig {
    
    private final RabbitMQProperties rabbitMQProperties;
    
    @Bean
    public Queue xpaymentQueue() {
        return QueueBuilder.durable(rabbitMQProperties.queueName())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.dlxExchangeName())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.dlxRoutingKey())
                .build();
    }
    
    @Bean
    public CustomExchange delayedExchange() {
        return new CustomExchange(
                rabbitMQProperties.delayedExchangeName(),
                "x-delayed-message",
                true,
                false,
                Map.of("x-delayed-type", "direct")
        );
    }
    
    @Bean
    public Binding queueBinding(Queue xpaymentQueue, CustomExchange delayedExchange) {
        return BindingBuilder
                .bind(xpaymentQueue)
                .to(delayedExchange)
                .with(rabbitMQProperties.queueName())
                .noargs();
    }
}
