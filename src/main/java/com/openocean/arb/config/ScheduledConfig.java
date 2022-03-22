package com.openocean.arb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时器配置
 * 解决Sping不能同时使用websocket（@EnableWebSocket）和spring的定时注解（@EnableScheduling）问题
 *
 * @author lidong
 **/
@Configuration
public class ScheduledConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolScheduler = new ThreadPoolTaskScheduler();
        threadPoolScheduler.setThreadNamePrefix("SockJS-");
        threadPoolScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolScheduler.setRemoveOnCancelPolicy(true);
        threadPoolScheduler.initialize();
        return threadPoolScheduler;
    }
}
