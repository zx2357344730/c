package com.cresign.chat.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 开启WebSocket支持
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注入websocket支持类
     * ##return:  实现类
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }


}
