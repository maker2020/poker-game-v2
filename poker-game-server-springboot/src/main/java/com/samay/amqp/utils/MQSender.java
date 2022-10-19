package com.samay.amqp.utils;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.samay.amqp.config.RabbitmqConfig;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MQSender {
    
    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitmqConfig rabbitmqConfig;

    @Autowired
    public MQSender(AmqpAdmin amqpAdmin,RabbitTemplate rabbitTemplate,RabbitmqConfig rabbitmqConfig){
        log.info("MQSender is initializing...");
        this.amqpAdmin=amqpAdmin;
        this.rabbitTemplate=rabbitTemplate;
        this.rabbitmqConfig=rabbitmqConfig;
        log.info("dependencies injected into MQSender succuessfully.");
    }

    public <T> void send(String exchangeName,T msg){
        FanoutExchange exchange=new FanoutExchange(exchangeName);
        Queue queue=rabbitmqConfig.queueMessage();
        amqpAdmin.declareQueue(queue);
        Binding binding=BindingBuilder.bind(queue).to(exchange);
        amqpAdmin.declareBinding(binding);
        if(msg instanceof String){
            MessageProperties messageProperties=new MessageProperties();
            // 设置消息内容的类型，默认application/octet-stream(且为Ascii编码)
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
            Message message=new Message(((String)msg).getBytes(),messageProperties);
            rabbitTemplate.convertAndSend("exchange", queue.getName(), message);
        }else{ // if
            rabbitTemplate.convertAndSend("exchange", queue.getName(), msg);
        }
    }

    /**
     * 删除一个Queue
     * @param queueName
     * @return
     */
    public boolean deleteQueue(String queueName){
        return amqpAdmin.deleteQueue(queueName);
    }
    
}
