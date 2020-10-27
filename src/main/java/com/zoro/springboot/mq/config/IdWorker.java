package com.zoro.springboot.mq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/26 1:34
 * @desc
 */
@Configuration
@ConfigurationProperties(prefix = "snowflake")
public class IdWorker {

    @Value("${snowflake.workerId}")
   private long workerId;

    @Value("${snowflake.dataCenterId}")
   private long dataCenterId;

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker(){
       return new SnowflakeIdWorker(workerId,dataCenterId);
    }
}
