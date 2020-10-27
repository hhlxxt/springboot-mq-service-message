package com.zoro.springboot.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootMqApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootMqApp.class,args);
    }
}
