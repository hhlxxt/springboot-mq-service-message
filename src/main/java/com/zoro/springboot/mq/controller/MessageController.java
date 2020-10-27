package com.zoro.springboot.mq.controller;

import com.zoro.springboot.mq.entity.RpTransactionMessage;
import com.zoro.springboot.mq.service.MessageSendHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 11:43
 * @desc
 */
@RestController
@Slf4j
public class MessageController {

    @Autowired
    private MessageSendHandleService messageSendHandleService ;

    @RequestMapping("/send")
    public String sendMsg(RpTransactionMessage rpTransactionMessage ){
        log.debug(rpTransactionMessage.toString());
        messageSendHandleService.saveMessageWaitingConfirm(rpTransactionMessage);
        log.debug("业务逻辑开始");
        try{
            messageSendHandleService.confirmAndSendMessage("3");
        }catch (Exception e){
            log.debug("发送消息失败，任务处理");
        }
        return "succ";
    }

}
