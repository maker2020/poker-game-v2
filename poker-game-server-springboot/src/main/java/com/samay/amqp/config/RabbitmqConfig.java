package com.samay.amqp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    
    private final static String message="web.socket.message";
    private final static String messages="send.socket.message";

    @Bean
    public Queue queueMessage(){
        return new Queue(RabbitmqConfig.message);
    }

    @Bean
    public Queue queueMessages(){
        return new Queue(RabbitmqConfig.messages);
    }

    @Bean
    TopicExchange exchange(){
        return new TopicExchange("exchange");
    }

    @Bean
    Binding bindingExchangeMessages(Queue queueMessages,TopicExchange exchange){
        return BindingBuilder.bind(queueMessages).to(exchange).with("send.#");
    }

}
