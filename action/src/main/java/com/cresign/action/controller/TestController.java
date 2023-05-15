package com.cresign.action.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.client.WSClient;
import com.cresign.tools.apires.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName TestController
 * @Date 2023/5/15
 * @ver 1.0.0
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private WSClient wsClient;

    @PostMapping("/v1/testFill")
    public JSONObject testFill(@RequestBody JSONObject reqJson){
        System.out.println("进入主的Test:");
        System.out.println(JSON.toJSONString(reqJson));
        reqJson.put("name","111");
        reqJson.put("age",20);
        return wsClient.testFill(reqJson);
    }

}
