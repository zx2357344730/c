package com.cresign.login.client;


import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.login.service.fallback.WSFilterFallbackFactory;

import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@FeignClient(
        value = "cresign-chat",
        fallbackFactory = WSFilterFallbackFactory.class,
        contextId = "WSFilterClient"
)

public interface WSFilterClient {

    //发送消息体
    @PostMapping("log/v1/sendLoginDesc")
    void sendLoginDesc(@RequestBody JSONObject reqJson);

    //发送消息体
    @PostMapping("log/v1/sendWS")
    void sendWS(@RequestBody LogFlow reqJson);


}
