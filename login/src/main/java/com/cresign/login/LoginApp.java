package com.cresign.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


/**
*##description:

*@author           JackSon
*@updated             2020/7/24 10:41
*/
@ComponentScan({"com.cresign.tools", "com.cresign.login"})
@SpringBootApplication
// 开启nacos的客户端
@EnableDiscoveryClient
// 开启feign的客户端
@EnableFeignClients
@EnableAsync

public class LoginApp {

    public static void main(String[] args) {
        SpringApplication.run(LoginApp.class, args);
    }



}
