package com.cresign.timer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
*##description:
*##Params:
*##Return:
*##author:           JackSon
*##updated:             2020/7/24 10:41
*/
// 定义哪些包需要被扫描,一旦指定了，Spring将会将在被指定的包及其下级的包(sub packages)中寻找bean
@ComponentScan({"com.cresign.tools", "com.cresign.timer"})
@SpringBootApplication
// 开启nacos的客户端
@EnableDiscoveryClient
// 开启feign的客户端
@EnableFeignClients
//开启任务调度，也就是开启定时任务
@EnableScheduling

@EnableAsync
public class TimerApp {

    public static void main(String[] args) {
        SpringApplication.run(TimerApp.class, args);
    }


}
