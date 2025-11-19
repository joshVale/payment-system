package com.example.xpaymentadapterapp.checkstate;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqDlxConfig {
    
    @Value("${app.rabbitmq.dlx-exchange-name}")
    private String dlxExchangeName;
    
    @Value("${app.rabbitmq.dlx-routing-key}")
    private String dlxRoutingKey;
    
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName);
    }
    
    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(dlxExchangeName + ".queue").build();
    }
    
    @Bean
    Binding dlxBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlxRoutingKey);
    }
}