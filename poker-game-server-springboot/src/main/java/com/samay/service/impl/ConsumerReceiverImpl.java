package com.samay.service.impl;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.samay.game.bo.Poker;
import com.samay.service.ConsumerReceiver;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsumerReceiverImpl implements ConsumerReceiver {

    @Override
    public void receiveJson(Message message, Channel channel) throws Exception {
        // 确认的唯一标识
        long deliveryTag=message.getMessageProperties().getDeliveryTag();
        try {
            Poker poker=JSON.parseObject(message.getBody(), Poker.class);
            log.info("consumer接收到消息: "+poker);
            // 手动确认（附上唯一标识，是否批处理deliveryTag所有消息）
            channel.basicAck(deliveryTag, true);
        } catch (Exception e) {
            // 手动拒绝(唯一标识，是否重发给其他consumer:false则删除)
            channel.basicReject(deliveryTag, false);
            e.printStackTrace();
        }
    } 

}
