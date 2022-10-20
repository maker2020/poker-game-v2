package com.samay.service;

import org.springframework.amqp.core.Message;

import com.rabbitmq.client.Channel;

public interface ConsumerReceiver {
    
    void receiveJson(Message message,Channel channel) throws Exception;

}
