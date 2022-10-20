package com.samay.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samay.amqp.config.RabbitmqConfig;
import com.samay.service.ProducerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProducerServiceImpl implements ProducerService{
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendTestJson(Object o) {
        rabbitTemplate.convertAndSend(RabbitmqConfig.DIRECT_EXCHANGE, RabbitmqConfig.DIRECT_ROUTING_KEY,o);
        log.info("producer 发送object成功.");
    }


}
