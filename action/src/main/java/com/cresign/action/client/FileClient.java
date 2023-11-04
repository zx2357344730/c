//package com.cresign.action.client;
//
//
//import com.alibaba.fastjson.JSONObject;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@FeignClient(
//        value = "cresign-file"
//)
//public interface FileClient {
//
//    //TODO KEV delete these
//
//    @PostMapping("encrypt/v1/delCOSFile")
//    long delCOSFile(JSONObject reqJson);
//
//    @PostMapping("encrypt/v1/cs")
//    Object cs(JSONObject json);
//}
