package com.cresign.tools.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName AsyncConfig
 * @Date 2023/9/19
 * @ver 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程大小
        threadPoolTaskExecutor.setCorePoolSize(8);
        // 最大线程大小
        threadPoolTaskExecutor.setMaxPoolSize(16);
        // 队列大小
        threadPoolTaskExecutor.setQueueCapacity(50);
        // 初始化
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
