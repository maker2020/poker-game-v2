package com.samay.amqp.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.samay.amqp.config.RabbitmqConfig;
import com.samay.service.ConsumerReceiver;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageListener implements ChannelAwareMessageListener{

    @Autowired
    private ConsumerReceiver consumerReceiver;

    @Override
    public void onMessage(Message message, @Nullable Channel channel) throws Exception {
        log.info("onMessage回调: 接收message成功.");
        // 获取队列名
        String queueName=message.getMessageProperties().getConsumerQueue();
        if(queueName.equals(RabbitmqConfig.DIRECT_QUEUE)){
            consumerReceiver.receiveJson(message, channel);
        }

    }
    
}
