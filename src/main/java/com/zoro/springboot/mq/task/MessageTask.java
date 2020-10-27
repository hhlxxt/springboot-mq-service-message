package com.zoro.springboot.mq.task;

import com.zoro.springboot.mq.service.MessageSendHandleService;
import com.zoro.springboot.mq.service.MessageTaskHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 12:07
 * @desc
 */
@Component
@Slf4j
public class MessageTask {

    @Resource(name ="taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private MessageTaskHandlerService messageTaskHandlerService ;

    //每隔两分钟执行一次
    @Scheduled(cron = "0 0/2 * * * ?")
    public void taskExecutor(){
        taskExecutor.submit(new Runnable() {
            @Override
            public void run() {
                messageTaskHandlerService.handleWaitingConfirmTimeOutMessages();
            }
        });

        taskExecutor.submit(new Runnable() {
            @Override
            public void run() {
                messageTaskHandlerService.handleSendingTimeOutMessage();
            }
        });

    }
}
