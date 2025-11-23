package com.example.xpaymentadapterapp.checkstate;

import com.example.xpaymentadapterapp.checkstate.config.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMqDlxConfig {
    
    private final RabbitMQProperties rabbitMQProperties;
    
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(rabbitMQProperties.dlxExchangeName());
    }
    
    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(rabbitMQProperties.dlxExchangeName() + ".queue").build();
    }
    
    @Bean
    Binding dlxBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(rabbitMQProperties.dlxRoutingKey());
    }
}