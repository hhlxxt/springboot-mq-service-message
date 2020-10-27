package com.zoro.springboot.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.zoro.springboot.mq.config.RabbitConfig;
import com.zoro.springboot.mq.entity.RpTransactionMessage;
import com.zoro.springboot.mq.service.MessageReceiverHandleService;
import com.zoro.springboot.mq.service.MessageSendHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 11:16
 * @desc
 */
@Component
@Slf4j
public class OrderListener {

    @Autowired
    private MessageReceiverHandleService messageReceiverHandleService ;

    @Autowired
    private MessageSendHandleService messageSendHandleService ;

    @RabbitHandler
    @RabbitListener(queues = RabbitConfig.SEND_MESSAGE_QUEUE)
    public void handler(String messageInfo , Message message , Channel channel) throws IOException {
        try {
            log.debug(String.valueOf(message));
            byte[] body = message.getBody();
            RpTransactionMessage rpTransactionMessage = JSONObject.parseObject(new String(body), RpTransactionMessage.class);
            //业务逻辑处理开始 需做幂等
            rpTransactionMessage.getField1();//该字段为对应的业务流水
            //业务逻辑处理结束
            //删除消息表中数据
            messageSendHandleService.deleteMessageByMessageId(rpTransactionMessage.getMessageId());
            //收到消息确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }catch (Exception e){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }


    }
}
