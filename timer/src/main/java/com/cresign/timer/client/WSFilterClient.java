package com.cresign.timer.client;


import com.cresign.timer.service.fallback.WSFilterFallbackFactory;
import com.cresign.tools.annotation.SecurityParameter;
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
    @SecurityParameter
    @PostMapping("log/v1/sendLogWS")
    void sendLogWS(@RequestBody LogFlow reqJson);

}
