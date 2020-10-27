package com.zoro.springboot.mq.service;

import com.zoro.springboot.mq.entity.RpTransactionMessage;
import com.zoro.springboot.mq.exception.MessageBizException;

import java.util.List;
import java.util.Map;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 10:44
 * @desc 消息处理接口类
 */
public interface MessageSendHandleService {
    /**
     * 预存储消息.
     */
    public int saveMessageWaitingConfirm(RpTransactionMessage rpTransactionMessage) throws MessageBizException;


    /**
     * 确认并发送消息.
     */
    public void confirmAndSendMessage(String messageId) throws MessageBizException;


    /**
     * 存储并发送消息.
     */
    public int saveAndSendMessage(RpTransactionMessage rpTransactionMessage) throws MessageBizException;


    /**
     * 直接发送消息.
     */
    public void directSendMessage(RpTransactionMessage rpTransactionMessage) throws MessageBizException;


    /**
     * 重发消息.
     */
    public void reSendMessage(RpTransactionMessage rpTransactionMessage) throws MessageBizException;


    /**
     * 根据messageId重发某条消息.
     */
    public void reSendMessageByMessageId(String messageId) throws MessageBizException;


    /**
     * 将消息标记为死亡消息.
     */
    public void setMessageToAreadlyDead(String messageId) throws MessageBizException;


    /**
     * 根据消息ID获取消息
     */
    public RpTransactionMessage getMessageByMessageId(String messageId) throws MessageBizException;

    /**
     * 根据消息ID删除消息
     */
    public void deleteMessageByMessageId(String messageId) throws MessageBizException;


    /**
     * 重发某个消息队列中的全部已死亡的消息.
     */
    public void reSendAllDeadMessageByQueueName(String queueName, int batchSize) throws MessageBizException;


    /**
     * 根据时间、状态、是否死亡(可选) 查询消息信息
     *
     * @param editTime
     * @param status
     * @param areadlyDead
     * @return
     */
    public List<RpTransactionMessage> customSelectPage(String editTime,String status ,String areadlyDead );

}
