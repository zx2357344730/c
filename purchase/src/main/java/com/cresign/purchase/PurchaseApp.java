package com.cresign.purchase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


/**
*##description:
*##Params:
*##Return:
*##author:           JackSon
*##updated:             2020/7/24 10:41
*/
@ComponentScan({"com.cresign.tools", "com.cresign.purchase"})
@SpringBootApplication
// 开启nacos的客户端
@EnableDiscoveryClient
// 开启feign的客户端
@EnableFeignClients
public class PurchaseApp {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseApp.class, args);
    }



}
