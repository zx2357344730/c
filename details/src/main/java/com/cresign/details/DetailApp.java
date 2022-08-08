package com.cresign.details;

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
@ComponentScan({"com.cresign.tools", "com.cresign.details"})
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class DetailApp {

    public static void main(String[] args) {
        SpringApplication.run(DetailApp.class, args);
    }

}
