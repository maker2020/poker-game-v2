package com.samay.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.samay.amqp.config.RabbitmqConfig;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RabbitListener(queues = RabbitmqConfig.DIRECT_QUEUE)
public class DefaultConsumer {
    
    @RabbitHandler
    public void receivedMessage(Message message,Channel channel){
        log.info("(@RabbitHandler) default consumer:接收消息成功");
    }

}
