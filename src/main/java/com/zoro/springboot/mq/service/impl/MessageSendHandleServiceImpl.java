package com.zoro.springboot.mq.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zoro.springboot.mq.config.RabbitConfig;
import com.zoro.springboot.mq.config.SnowflakeIdWorker;
import com.zoro.springboot.mq.dao.RpTransactionMessageDao;
import com.zoro.springboot.mq.entity.RpTransactionMessage;
import com.zoro.springboot.mq.enums.MessageStatusEnum;
import com.zoro.springboot.mq.enums.PublicEnum;
import com.zoro.springboot.mq.exception.MessageBizException;
import com.zoro.springboot.mq.service.MessageSendHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 10:44
 * @desc 消息处理实现类
 */
@Service("messageHandleService")
@Slf4j
public class MessageSendHandleServiceImpl implements MessageSendHandleService {

    @Autowired
    private AmqpTemplate amqpTemplate ;

    @Autowired
    private RpTransactionMessageDao rpTransactionMessageDao ;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker ;

    @Override
    public int saveMessageWaitingConfirm(RpTransactionMessage rpTransactionMessage) throws MessageBizException {
        log.debug("持久化数据");
        rpTransactionMessage.setMessageId(String.valueOf(snowflakeIdWorker.nextId()));
        rpTransactionMessage.setEditTime(new Date());
        rpTransactionMessage.setMessageBody(JSON.toJSONString("消息测试"));
        rpTransactionMessage.setField1("1003222");//预留字段 订单号
        rpTransactionMessage.setConsumerQueue(RabbitConfig.SEND_MESSAGE_QUEUE);
        return rpTransactionMessageDao.insert(rpTransactionMessage);
    }

    @Override
    public void confirmAndSendMessage(String messageId) throws MessageBizException {
        log.debug("消息确认并发送,messageId:{},开始",messageId);
        RpTransactionMessage message = getMessageByMessageId(messageId);
        if (message == null) {
            throw new MessageBizException(MessageBizException.SAVA_MESSAGE_IS_NULL, "根据消息id查找的消息为空");
        }

        Integer version = message.getVersion();
        UpdateWrapper<RpTransactionMessage> whereWrapper = new UpdateWrapper<>();
        whereWrapper.eq("message_Id",messageId).eq("version",version);//数据库字段

        message.setVersion(version+1);
        message.setStatus(MessageStatusEnum.SENDING.name());
        int result = rpTransactionMessageDao.update(message, whereWrapper);
        if(result != 1){
            throw new MessageBizException(MessageBizException.DB_INSERT_RESULT_0.getCode(), "消息确认后,更新消息为发送中失败,版本号已经发生变化");
        }
        amqpTemplate.convertAndSend(RabbitConfig.SEND_MESSAGE_QUEUE, JSON.toJSONString(message));
        log.debug("消息确认并发送,messageId:{},结束",messageId);

    }

    @Override
    public int saveAndSendMessage(RpTransactionMessage rpTransactionMessage) throws MessageBizException {
        return 0;
    }

    @Override
    public void directSendMessage(RpTransactionMessage rpTransactionMessage) throws MessageBizException {

    }

    @Override
    public void reSendMessage(RpTransactionMessage rpTransactionMessage) throws MessageBizException {
        log.debug("消息重发,messageId:{}",rpTransactionMessage.getMessageId());
        amqpTemplate.convertAndSend(RabbitConfig.SEND_MESSAGE_QUEUE,JSON.toJSONString(rpTransactionMessage));
    }

    @Override
    public void reSendMessageByMessageId(String messageId) throws MessageBizException {

    }

    @Override
    public void setMessageToAreadlyDead(String messageId) throws MessageBizException {
        log.debug("标注消息死亡,messageId:{}",messageId);
        RpTransactionMessage message = getMessageByMessageId(messageId);
        if (message == null) {
            throw new MessageBizException(MessageBizException.SAVA_MESSAGE_IS_NULL, "根据消息id查找的消息为空");
        }

        message.setAreadlyDead(PublicEnum.Y.name());
        message.setEditTime(new Date());
        message.setVersion(message.getVersion()+1);

        UpdateWrapper<RpTransactionMessage> whereWrapper = new UpdateWrapper<>();
        whereWrapper.eq("message_Id",messageId).eq("areadly_dead","N");//数据库字段

        int result = rpTransactionMessageDao.update(message,whereWrapper);
        if(result != 1){
            throw new MessageBizException(MessageBizException.DB_INSERT_RESULT_0.getCode(), "更新消息areadly_dead状态失败");
        }

    }

    @Override
    public RpTransactionMessage getMessageByMessageId(String messageId) throws MessageBizException {
        QueryWrapper<RpTransactionMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("message_Id" , messageId);
        return rpTransactionMessageDao.selectOne(queryWrapper);
    }

    @Override
    public void deleteMessageByMessageId(String messageId) throws MessageBizException {

        log.debug("根据messageId:{}删除消息",messageId);
        RpTransactionMessage message = getMessageByMessageId(messageId);
        if (message == null) {
            throw new MessageBizException(MessageBizException.SAVA_MESSAGE_IS_NULL, "根据消息id查找的消息为空");
        }
        Map<String, Object> columnMap = new HashMap<>(1);
        columnMap.put("message_Id",messageId);
        int result = rpTransactionMessageDao.deleteByMap(columnMap);
        if (result == 1){
            log.debug("根据messageId:{}删除消息,成功",messageId);
        }else{
            log.debug("根据messageId:{}删除消息,失败",messageId);
        }

    }

    @Override
    public void reSendAllDeadMessageByQueueName(String queueName, int batchSize) throws MessageBizException {

    }

    @Override
    public List<RpTransactionMessage> customSelectPage(String editTime, String status, String areadlyDead) {
        return rpTransactionMessageDao.customSelectPage(editTime, status, areadlyDead);
    }
}
