package com.zoro.springboot.mq.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zoro.springboot.mq.dao.RpTransactionMessageDao;
import com.zoro.springboot.mq.entity.RpTransactionMessage;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 11:21
 * @desc
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageSendHandleServiceTest {

    @Autowired
    private MessageSendHandleService messageSendHandleService ;

    @Autowired
    RpTransactionMessageDao rpTransactionMessageDao ;

    @Test
    public void confirmAndSendMessage() {
        messageSendHandleService.confirmAndSendMessage("");
    }


    @Test
    public void customSelectPage() {
        List<RpTransactionMessage> rpTransactionMessages = messageSendHandleService.customSelectPage("2020-10-25 21:46:34", "WAITING", "2");
        System.out.println(rpTransactionMessages);
    }

    @Test
    public void te(){
        QueryWrapper<RpTransactionMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status","WAITING");
        List<RpTransactionMessage> rpTransactionMessages = rpTransactionMessageDao.selectList(queryWrapper);
        System.out.println(rpTransactionMessages);
    }
}