package com.zoro.springboot.mq.service;

import com.zoro.springboot.mq.entity.RpTransactionMessage;
import com.zoro.springboot.mq.enums.MessageStatusEnum;
import com.zoro.springboot.mq.enums.PublicEnum;
import com.zoro.springboot.mq.utils.PublicConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 12:49
 * @desc  处理超时消息
 */
@Component
@Slf4j
public class MessageTaskHandlerService {

    @Autowired
    private MessageBizHandlerService messageBizHandlerService ;

    @Autowired
    private MessageSendHandleService messageSendHandleService ;

    /**
     * 处理状态为“待确认”但已超时的消息.
     */
    public void handleWaitingConfirmTimeOutMessages() {
        try {

            Map<String, Object> paramMap = new HashMap<String, Object>(); // 查询条件
            // 获取配置的开始处理的时间
            String dateStr = getCreateTimeBefore();
            Map<String, RpTransactionMessage> messageMap = getMessageMap("状态为“待确认”但已超时的消息",dateStr, MessageStatusEnum.WAITING.name(), "");

            messageBizHandlerService.handleWaitingConfirmTimeOutMessages(messageMap);

        } catch (Exception e) {
            log.error("处理[waiting_confirm]状态的消息异常" + e);
        }
    }


    /**
     * 处理状态为“发送中”但超时没有被成功消费确认的消息
     */
    public void handleSendingTimeOutMessage() {
        try {
            log.debug("处理状态为“发送中”但超时没有被成功消费确认的消息");
            // 获取配置的开始处理的时间
            String dateStr = getCreateTimeBefore();

            Map<String, RpTransactionMessage> messageMap = getMessageMap("状态为“发送中”但超时没有被成功消费确认的消息",dateStr, MessageStatusEnum.SENDING.name(),  PublicEnum.N.name());

            messageBizHandlerService.handleSendingTimeOutMessage(messageMap);
        } catch (Exception e) {
            log.error("处理发送中的消息异常" + e);
        }
    }


    private Map<String, RpTransactionMessage> getMessageMap(String msgType , String editTime,String status ,String areadlyDead ){

        Map<String, RpTransactionMessage> messageMap = new HashMap<String, RpTransactionMessage>(); // 转换成map
        List<RpTransactionMessage> recordList = messageSendHandleService.customSelectPage(editTime, status, areadlyDead);

        if (recordList == null || recordList.isEmpty()) {
            log.info("==>{} is empty",msgType);
            return messageMap;
        }
        log.info("==>now page size:" + recordList.size());

        for (RpTransactionMessage message : recordList) {
            messageMap.put(message.getMessageId(), message);
        }
        return messageMap;
    }

    /**
     * 获取配置的开始处理的时间
     *
     * @return
     */
    private String getCreateTimeBefore() {
        String duration = PublicConfigUtil.readConfig("message.handle.duration");
        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        Date date = new Date(currentTimeInMillis - Integer.valueOf(duration) * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        return dateStr;
    }

}
