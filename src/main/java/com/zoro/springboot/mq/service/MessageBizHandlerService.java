package com.zoro.springboot.mq.service;

import com.zoro.springboot.mq.entity.RpTransactionMessage;
import com.zoro.springboot.mq.utils.PublicConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 13:02
 * @desc
 */
@Component
@Slf4j
public class MessageBizHandlerService {

    @Autowired
    private MessageSendHandleService messageSendHandleService ;

    /**
     * 处理[waiting_confirm]状态的消息
     *
     * @param messageMap
     */
    public void handleWaitingConfirmTimeOutMessages(Map<String, RpTransactionMessage> messageMap) {
        log.debug("开始处理[waiting_confirm]状态的消息,总条数[{}]",messageMap.size());
        // 单条消息处理（目前该状态的消息，消费队列全部是accounting，如果后期有业务扩充，需做队列判断，做对应的业务处理。）
        for (Map.Entry<String, RpTransactionMessage> entry : messageMap.entrySet()) {
            RpTransactionMessage message = entry.getValue();
            try {

                String messageId = message.getMessageId();
                log.debug("开始处理[waiting_confirm]消息,messageId为[{}]的消息", messageId);
                String bankOrderNo = message.getField1();
                //如果订单成功，把消息改为待处理，并发送消息 TradeStatusEnum.SUCCESS.name().equals(record.getStatus())
                //通过bankOrderNo查询到对应的订单状态 此处伪代码 messageid对2取模 ==0 订单成功 否则为超时订单
                if (Long.parseLong(messageId)%2 == 0) {
                    // 确认并发送消息
                    messageSendHandleService.confirmAndSendMessage(message.getMessageId());

                } else if (Long.parseLong(messageId)%2 != 0) {//TradeStatusEnum.WAITING_PAYMENT.name().equals(record.getStatus())
                    // 订单状态是等到支付，可以直接删除数据
                    log.debug("订单没有支付成功,删除[waiting_confirm]消息id[{}]的消息", message.getMessageId());
                    messageSendHandleService.deleteMessageByMessageId(message.getMessageId());
                }

                log.debug("结束处理[waiting_confirm]消息ID为[{}]的消息",message.getMessageId());
            } catch (Exception e) {
                log.error("处理[waiting_confirm]消息ID为[{}]的消息异常：", message.getMessageId() ,e);
            }
        }
    }

    /**
     * 处理[SENDING]状态的消息
     *
     * @param messageMap
     */
    public void handleSendingTimeOutMessage(Map<String, RpTransactionMessage> messageMap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.debug("开始处理[SENDING]状态的消息,总条数[{}]",messageMap.size() );

        // 根据配置获取通知间隔时间
       Map<Integer, Integer> notifyParam = getSendTime();

        // 单条消息处理
        for (Map.Entry<String, RpTransactionMessage> entry : messageMap.entrySet()) {
            RpTransactionMessage message = entry.getValue();
            try {
                log.debug("开始处理[SENDING]消息ID为[{}]的消息", message.getMessageId());
                // 判断发送次数
                int maxTimes = Integer.valueOf(PublicConfigUtil.readConfig("message.max.send.times"));
                log.debug("[SENDING]消息ID为[{}]的消息,已经重新发送的次数[{}]", message.getMessageId(), message.getMessageSendTimes());

                // 如果超过最大发送次数直接退出
                if (maxTimes < message.getMessageSendTimes()) {
                    // 标记为死亡
                    messageSendHandleService.setMessageToAreadlyDead(message.getMessageId());
                    continue;
                }
                // 判断是否达到发送消息的时间间隔条件
                int reSendTimes = message.getMessageSendTimes();
                int times = notifyParam.get(reSendTimes == 0 ? 1 : reSendTimes);
                long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
                long needTime = currentTimeInMillis - times * 60 * 1000;
                long hasTime = message.getEditTime().getTime();
                // 判断是否达到了可以再次发送的时间条件
                if (hasTime > needTime) {
                    log.debug("currentTime[{}],[SENDING]消息上次发送时间[{}],必须过了[{}]分钟才可以再发送。",sdf.format(new Date()),sdf.format(message.getEditTime()), times);
                    continue;
                }

                // 重新发送消息
                messageSendHandleService.reSendMessage(message);

                log.debug("结束处理[SENDING]消息ID为[{}]的消息", message.getMessageId() );
            } catch (Exception e) {
                log.error("处理[SENDING]消息ID为[{}]的消息异常：", message.getMessageId(), e);
            }
        }

    }

    /**
     * 根据配置获取通知间隔时间
     *
     * @return
     */
    private Map<Integer, Integer> getSendTime() {
        Map<Integer, Integer> notifyParam = new HashMap<Integer, Integer>();
        notifyParam.put(1, Integer.valueOf(PublicConfigUtil.readConfig("message.send.1.time")));
        notifyParam.put(2, Integer.valueOf(PublicConfigUtil.readConfig("message.send.2.time")));
        notifyParam.put(3, Integer.valueOf(PublicConfigUtil.readConfig("message.send.3.time")));
        notifyParam.put(4, Integer.valueOf(PublicConfigUtil.readConfig("message.send.4.time")));
        notifyParam.put(5, Integer.valueOf(PublicConfigUtil.readConfig("message.send.5.time")));
        return notifyParam;
    }

}
