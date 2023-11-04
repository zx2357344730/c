//package com.cresign.action.client;
//
//import com.cresign.action.service.fallback.WSFallbackFactory;
//import com.cresign.tools.pojo.po.LogFlow;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//
//@FeignClient(
//        value = "cresign-chat",
//        fallbackFactory = WSFallbackFactory.class,
//        contextId = "WSClient"
//)
//
//public interface WSClient {
//
//    //TODO KEV Delete?
//
//    //发送消息体 //
//    @PostMapping("log/v1/sendWS")
//    void sendWS(@RequestBody LogFlow reqJson);
//
//
//}
