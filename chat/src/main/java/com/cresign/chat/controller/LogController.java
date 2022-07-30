package com.cresign.chat.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.config.websocket.WebSocketLoginServer;
import com.cresign.chat.config.websocket.WebSocketServerPi;
import com.cresign.chat.config.websocket.WebSocketUserServer;
import com.cresign.chat.service.LogService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 日志接口提供类
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private LogService logService;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private HttpServletRequest request;

    /**
     * 发送
     * ##param can 请求参数
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/30 16:13
     */
    @PostMapping("/v1/sendLoginDesc")
    public void sendLoginDesc(@RequestBody JSONObject reqJson){
        WebSocketLoginServer.sendInfo(
                reqJson.getString("id"),
                reqJson.getJSONObject("infoData"));
    }

    /**
     * 发送
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/30 16:13
     */
    @PostMapping("/v1/sendWS")
    public void sendLogWS(@RequestBody LogFlow logData){
        WebSocketUserServer.sendLog(logData);
    }

    /**
     * 发送
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/30 16:13
     */
    @PostMapping("/v1/sendWSPi")
    public void sendLogWSPi(@RequestBody LogFlow logData){

        WebSocketServerPi.sendInfo(logData);
    }


    /**
     * 根据reqJson获取单个用户信息
     * ##Params: reqJson	请求参数
     * ##return: java.lang.String  返回结果:用户信息
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 8:21
     */
    @SecurityParameter
    @PostMapping("/v1/setGpio")
    public ApiResponse setGpio(@RequestBody JSONObject reqJson){

        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        // 获取用户id
//        String uid = reqJson.getString(ChatConstants.L_C_REQUEST_UID);
//        String cid = reqJson.getString(ChatConstants.L_C_REQUEST_CID);
//        String gpIo = reqJson.getString("gpIo");
//        String rname = reqJson.getString("rname");
        reqJson.put("id_U",tokData.getString("id_U"));
        System.out.println("进入绑定");
        return logService.setGpio(reqJson);
    }

    /**
     * 根据reqJson获取单个用户信息
     * ##Params: reqJson	请求参数
     * ##return: java.lang.String  返回结果:用户信息
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 8:21
     */
    @SecurityParameter
    @PostMapping("/v1/unsetGpio")
    public ApiResponse unsetGpio(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        // 获取用户id
//        String uid = reqJson.getString(ChatConstants.L_C_REQUEST_UID);
//        String cid = reqJson.getString(ChatConstants.L_C_REQUEST_CID);
//        String gpIo = reqJson.getString("gpIo");
//        String rname = reqJson.getString("rname");
        reqJson.put("id_U",tokData.getString("id_U"));
        System.out.println("进入解除绑定");
        return logService.unsetGpio(reqJson);
    }

//    /**
//     * 根据reqJson获取单个用户信息
//     * ##Params: reqJson	请求参数
//     * ##return: java.lang.String  返回结果:用户信息
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/6 8:21
//     */
//    @SecurityParameter
//    @PostMapping("/v1/getIdCAndRpI")
//    public ApiResponse getIdCAndRpI(@RequestBody JSONObject reqJson){
//
//        // 获取用户id
////        String uid = reqJson.getString(ChatConstants.L_C_REQUEST_UID);
//        return logService.getIdCAndRpI();
//    }

}
