package com.cresign.action.client;

import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.fallback.WSFallbackFactory;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        value = "cresign-chat",
        fallbackFactory = WSFallbackFactory.class,
        contextId = "WSClient"
)

public interface WSClient {


    //发送消息体 //
    @PostMapping("log/v1/sendWS")
    void sendWS(@RequestBody LogFlow reqJson);

    @PostMapping("log/v1/testFill")
    JSONObject testFill(@RequestBody JSONObject object);

}
