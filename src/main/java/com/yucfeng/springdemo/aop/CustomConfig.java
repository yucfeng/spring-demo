package com.yucfeng.springdemo.aop;

import com.yucfeng.springdemo.service.KVService;
import com.yucfeng.springdemo.service.KVServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class CustomConfig {

    @Bean
    public CustomLog customLog(){
        return new CustomLog();
    }

    @Bean
    public KVService kvService(){
        return new KVServiceImpl();
    }

}
