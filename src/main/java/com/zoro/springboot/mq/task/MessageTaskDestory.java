package com.zoro.springboot.mq.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 12:41
 * @desc
 */
@Component
@Slf4j
public class MessageTaskDestory {

    @Resource(name ="taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @PreDestroy
    public void destory(){
        log.debug("线程池销毁");
        taskExecutor.destroy();
    }
}
