package com.samay.amqp.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.samay.amqp.listener.MessageListener;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>Rabbitmq配置类:</b>
 * 包含了发送和接收的公共配置<p>
 * Rabbitmq的交换机、队列、路由key、绑定器、监听者、连接工厂、消息转换器等Bean注册,<p>
 * 以及rabbitTemplate的自定义Bean。<p>
 * <hr>
 * 使用rabbitTemplate的时候如果没有特别指定，那么相关属性都为该配置类的属性。
 */
@Configuration
@Slf4j
@Data
public class RabbitmqConfig {

    /**
     * Direct 队列名称
     */
    public static final String DIRECT_QUEUE = "direct_queue";

    /**
     * Direct 交换机名称
     */
    public static final String DIRECT_EXCHANGE = "direct_exchange";

    /**
     * Direct 交换机-队列 routing-key
     */
    public static final String DIRECT_ROUTING_KEY = "direct_routing_key";

    @Autowired
    private MessageListener messageReceiver;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    /**
     * 自定义rabbitTemplate的bean
     * 
     * @return
     */
    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        //
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);

        // 设置json转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 设置开启Mandatory(强制回调)
        rabbitTemplate.setMandatory(true);
        // 确认消息发送至交换机 的回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("correlationData: " + correlationData);
            log.info("ack: " + ack);
            log.info("cause: " + cause);
        });
        // 确认消息发送至队列 的回调
        rabbitTemplate.setReturnsCallback((result)->{
            log.info("msg: "+result.getMessage());
            log.info("replyCode: "+result.getReplyCode());
            log.info("replyText: "+result.getReplyText());
            log.info("exchange: "+result.getExchange());
            log.info("routing-key: "+result.getRoutingKey());
        });
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Direct 交换机
     * @return
     */
    @Bean
    public DirectExchange directExchange(){
        /**
         * 交换机名，是否持久化磁盘，是否自动删除
         */
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    /**
     * Direct 队列
     * @return
     */
    @Bean
    public Queue directQueue(){
        /**
         * 队列名，是否持久化，是否排他性，是否自动删除(无生产/消费使用则删除)，其他自定义参数
         */
        return new Queue(DIRECT_QUEUE, true, false, false, null);
    }

    @Bean
    Binding directBinding(DirectExchange directExchange,Queue directQueue){
        return BindingBuilder.bind(directQueue).to(directExchange).with(DIRECT_ROUTING_KEY);
    }

    /**
     * 自定义简单消息监听容器
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){
        // 消费者数量，默认10
        final int DEFAULT_CONCUREENT=10;
        // 每个消费者最大投递数量，默认50
        final int DEFAULT_PREFETCH_COUNT=50;
        
        SimpleMessageListenerContainer container=new SimpleMessageListenerContainer(connectionFactory);
        container.setConcurrentConsumers(DEFAULT_CONCUREENT);
        container.setMaxConcurrentConsumers(DEFAULT_PREFETCH_COUNT);
        
        // RabbitMQ自动确认改为手动确认
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 添加队列，可添加多个队列
        container.addQueues(new Queue(DIRECT_QUEUE,true));

        // 设置监听处理类
        container.setMessageListener(messageReceiver);

        return container;
    }

}
