package com.zoro.springboot.mq.config;
 
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 

@Slf4j
@Configuration
public class RabbitConfig {
    public final static String SEND_MESSAGE_QUEUE = "send-message-queue";
    //交换器
    public final static String TOPIC_EXCHANGE = "topicExchange";

    /**
     * 测试队列实例
     */
    @Bean
    public Queue testQueue() {
        log.info("测试队列实例创建成功");
        return new Queue("test-queue");
    }

 
    /**
     * 发送消息队列实例，并持久化
     */
    @Bean
    public Queue sendMessageQueue() {
        Queue sendMessageQueue = new Queue(SEND_MESSAGE_QUEUE, true);
        log.info("发送消息队列实例创建成功");
        return sendMessageQueue;
    }

    /**
     * 创建Topic Exchange交换机也叫通配符交换机
     * <p>
     * Topic Exchange主要有两种通配符：# 和 *
     * *（星号）：可以（只能）匹配一个单词
     * #（井号）：可以匹配多个单词（或者零个）
     */
    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    /**
     * 绑定发送消息队列到交换机
     */
    @Bean
    public Binding sendMessageBinding() {
        Binding binding = BindingBuilder.bind(sendMessageQueue()).to(topicExchange()).with("order.message.*");
        log.info("发送消息队列与交换机绑定成功");
        return binding;
    }

}